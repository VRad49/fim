/*
 * This file is part of Fim - File Integrity Manager
 *
 * Copyright (C) 2015  Etienne Vrignaud
 *
 * Fim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Fim.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fim.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.fim.model.Context;
import org.fim.model.FileToIgnore;
import org.fim.model.FimIgnore;
import org.fim.util.FileUtil;
import org.fim.util.Logger;

public class FimIgnoreManager
{
	public static final String DOT_FIM_IGNORE = ".fimignore";
	public static final String ALL_DIRECTORIES_PATTERN = "**/";

	public static final Set IGNORED_DIRECTORIES = new HashSet<>(Arrays.asList(Context.DOT_FIM_DIR, ".git", ".svn", ".cvs"));

	private final Context context;

	private String repositoryRootDirString;
	private Set<String> ignoredFiles;

	public FimIgnoreManager(Context context)
	{
		this.context = context;
		this.repositoryRootDirString = this.context.getRepositoryRootDir().toString();
		this.ignoredFiles = new HashSet<>();
	}

	public FimIgnore loadInitialFimIgnore()
	{
		FimIgnore initialFimIgnore = loadGlobalFimIgnore();
		addParentFimIgnore(initialFimIgnore);
		return initialFimIgnore;
	}

	public FimIgnore loadLocalIgnore(Path directory, FimIgnore parentFimIgnore)
	{
		FimIgnore fimIgnore = loadFimIgnore(directory);
		fimIgnore.getFilesToIgnoreInAllDirectories().addAll(parentFimIgnore.getFilesToIgnoreInAllDirectories());
		return fimIgnore;
	}

	/**
	 * If Fim is started from a sub-directory, it loads the parent .fimignore files and merge all the filesToIgnoreInAllDirectories.
	 */
	private void addParentFimIgnore(FimIgnore initialFimIgnore)
	{
		Path directory = Paths.get(".").toAbsolutePath().normalize();
		while (false == directory.equals(context.getRepositoryRootDir()))
		{
			directory = directory.getParent();
			if (directory == null)
			{
				break;
			}

			FimIgnore fimIgnore = loadFimIgnore(directory);
			initialFimIgnore.getFilesToIgnoreInAllDirectories().addAll(fimIgnore.getFilesToIgnoreInAllDirectories());
		}
	}

	private FimIgnore loadGlobalFimIgnore()
	{
		Path userDir = Paths.get(System.getProperty("user.dir"));
		return loadFimIgnore(userDir);
	}

	protected FimIgnore loadFimIgnore(Path directory)
	{
		FimIgnore fimIgnore = new FimIgnore();

		Path dotFimIgnore = directory.resolve(DOT_FIM_IGNORE);
		if (Files.exists(dotFimIgnore))
		{
			try
			{
				List<String> allLines = Files.readAllLines(dotFimIgnore);
				for (String line : allLines)
				{
					if (line.startsWith(ALL_DIRECTORIES_PATTERN))
					{
						String fileNamePattern = line.substring(ALL_DIRECTORIES_PATTERN.length());
						FileToIgnore fileToIgnore = new FileToIgnore(fileNamePattern);
						fimIgnore.getFilesToIgnoreInAllDirectories().add(fileToIgnore);
					}
					else
					{
						FileToIgnore fileToIgnore = new FileToIgnore(line);
						fimIgnore.getFilesToIgnoreLocally().add(fileToIgnore);
					}
				}
			}
			catch (IOException e)
			{
				Logger.error(String.format("Unable to read file %s: %s", dotFimIgnore, e.getMessage()));
			}
		}

		return fimIgnore;
	}

	// -----------------------------------------------------------------------------------------------------------------

	public boolean isIgnored(Path file, BasicFileAttributes attributes, FimIgnore fimIgnore)
	{
		String fileName = file.getFileName().toString();
		if (attributes.isDirectory() && IGNORED_DIRECTORIES.contains(fileName))
		{
			return true;
		}

		if (isIgnored(fileName, fimIgnore.getFilesToIgnoreInAllDirectories()))
		{
			return true;
		}

		if (isIgnored(fileName, fimIgnore.getFilesToIgnoreLocally()))
		{
			return true;
		}

		return false;
	}

	private boolean isIgnored(String fileName, Set<FileToIgnore> filesToIgnore)
	{
		for (FileToIgnore fileToIgnore : filesToIgnore)
		{
			if (fileToIgnore.getCompiledPattern() != null)
			{
				Matcher matcher = fileToIgnore.getCompiledPattern().matcher(fileName);
				if (matcher.find())
				{
					return true;
				}
			}
			else if (fileToIgnore.getFileNamePattern().equals(fileName))
			{
				return true;
			}
		}
		return false;
	}

	public void ignoreThisFiles(Path file, BasicFileAttributes attributes)
	{
		String normalizedFileName = FileUtil.getNormalizedFileName(file);
		if (attributes.isDirectory())
		{
			normalizedFileName = normalizedFileName + "/";
		}

		String relativeFileName = FileUtil.getRelativeFileName(repositoryRootDirString, normalizedFileName);
		ignoredFiles.add(relativeFileName);
	}

	public Set<String> getIgnoredFiles()
	{
		return ignoredFiles;
	}
}
