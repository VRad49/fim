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
package org.fim.internal.hash;

import static java.lang.Math.min;
import static org.fim.model.Contants._1_MB;
import static org.fim.model.HashMode.hashAll;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

import org.fim.model.HashMode;
import org.fim.model.Range;
import org.fim.util.HashModeUtil;

public class FullHasher extends AbstractHasher
{
	public static final int BLOCK_SIZE = 30 * _1_MB;

	private long fileSize;

	public FullHasher(HashMode hashMode) throws NoSuchAlgorithmException
	{
		super(hashMode);
	}

	@Override
	protected boolean isCompatible(HashMode hashMode)
	{
		return HashModeUtil.isCompatible(hashMode, hashAll);
	}

	@Override
	public void reset(long fileSize)
	{
		super.reset(fileSize);

		if (isActive())
		{
			this.fileSize = fileSize;
		}
	}

	@Override
	public Range getNextRange(long filePosition)
	{
		long from = filePosition;
		long to = min(fileSize, filePosition + BLOCK_SIZE);
		return new Range(from, to);
	}

	@Override
	protected ByteBuffer getNextBlockToHash(long filePosition, long currentPosition, ByteBuffer buffer)
	{
		return buffer;
	}

	@Override
	public boolean hashComplete()
	{
		if (isActive())
		{
			return getBytesHashed() == fileSize;
		}
		else
		{
			return true;
		}
	}
}
