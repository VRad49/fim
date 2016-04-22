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
package org.fim.util;

import static org.assertj.core.api.Assertions.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.fim.model.Attribute;
import org.fim.model.FileHash;
import org.fim.model.FileState;
import org.fim.model.FileTime;
import org.junit.Before;
import org.junit.Test;

public class FormatUtilTest
{
	Calendar calendar;
	FileState fileState;

	@Before
	public void setup()
	{
		int year = 115;
		int month = 11;
		int day = 14;
		int hrs = 13;
		int min = 12;
		int seconds = 10;
		calendar = new GregorianCalendar(year + 1900, month - 1, day, hrs, min, seconds);

		long fileLength = 512;
		FileTime fileTime = new FileTime(0, 0);
		FileHash fileHash = new FileHash("", "", "");
		List<Attribute> attributeList = null;
		fileState = new FileState("fileName", fileLength, fileTime, fileHash, attributeList);
	}

	@Test
	public void weCanFormatADate()
	{
		assertThat(FormatUtil.formatDate(calendar.getTimeInMillis())).isEqualTo("2015/11/14 13:12:10");
	}

	@Test
	public void weCanFormatCreationTime()
	{
		fileState.getFileTime().setCreationTime(calendar.getTimeInMillis());
		assertThat(FormatUtil.formatCreationTime(fileState)).isEqualTo("2015/11/14 13:12:10");
	}

	@Test
	public void weCanFormatLastModified()
	{
		fileState.getFileTime().setLastModified(calendar.getTimeInMillis());
		assertThat(FormatUtil.formatLastModified(fileState)).isEqualTo("2015/11/14 13:12:10");
	}
}
