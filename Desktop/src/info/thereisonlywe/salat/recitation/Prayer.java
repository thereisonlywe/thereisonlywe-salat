package info.thereisonlywe.salat.recitation;

import info.thereisonlywe.core.essentials.MathEssentials;
import info.thereisonlywe.quran.Quran;
import info.thereisonlywe.quran.QuranicVerse;

import java.util.ArrayList;

/**
 * 
 * @author thereisonlywe
 * @since October 28th 2011
 * @version Jan 1st 2013
 */

public class Prayer {

	public static final int VERSE_RANGE_TRUE_MOHAMMEDAN = 4;

	public static final int VERSE_RANGE_MOHAMMEDAN = 3;

	public static final int VERSE_RANGE_WIDE = 2;

	public static final int VERSE_RANGE_COMMON = 1;

	public static final int VERSE_RANGE_NARROW = 0;

	public static final int SALAT_RANDOMIZATION_FULL = 2;

	public static final int SALAT_RANDOMIZATION_IN_SECTION = 1;

	public static final int SALAT_RANDOMIZATION_SINGLE = 0;

	private final int numberOfRakaat;

	private int[] pauseIntervals = new int[] { 7000, 2000, 3000, 12000, 5000,
			9000, 5000, 7000, 5000, 15000, 40000, 3000 };

	private int pauseRandomizationInterval = 1500;

	private int rakaatPointer = 0;

	private boolean reciteDhikr = false; // after-salat dhikr

	private boolean reciteIqama = true;

	private final QuranReciter reciter;

	private final int salatRandomizationMode;

	private final ArrayList<QuranicVerse[]> verseList = new ArrayList<QuranicVerse[]>();

	private final int verseRange;

	public Prayer(final int verseRange, final int numberOfRakaat,
			final QuranReciter reciter, final int salatRandomizationMode)
	{
		this.verseRange = verseRange;
		this.numberOfRakaat = numberOfRakaat;
		this.reciter = reciter;
		this.salatRandomizationMode = salatRandomizationMode;
		this.pauseRandomizationInterval = MathEssentials
				.getMinValue(pauseIntervals) / 2;

		initVerseList();
	}

	public Prayer(final int verseRange, final int numberOfRakaat,
			final QuranReciter reciter, final int salatRandomizationMode,
			final boolean reciteIqama)
	{
		this(verseRange, numberOfRakaat, reciter, salatRandomizationMode);
		setIqamaRecitation(reciteIqama);
	}

	public Prayer(final int verseRange, final int numberOfRakaat,
			final QuranReciter reciter, final int salatRandomizationMode,
			final boolean reciteIqama, final boolean reciteDhikr)
	{
		this(verseRange, numberOfRakaat, reciter, salatRandomizationMode,
				reciteIqama);
		setDhikrRecitation(reciteDhikr);
	}

	public Prayer(final int verseRange, final int numberOfRakaat,
			final QuranReciter reciter, final int salatRandomizationMode,
			final boolean reciteIqama, final boolean reciteDhikr,
			final int pauseRandomizationInterval)
	{
		this(verseRange, numberOfRakaat, reciter, salatRandomizationMode,
				reciteIqama, reciteDhikr);
		setPauseRandomizationInterval(pauseRandomizationInterval);
	}

	public Prayer(final int verseRange, final int numberOfRakaat,
			final QuranReciter reciter, final int salatRandomizationMode,
			final boolean reciteIqama, final boolean reciteDhikr,
			final int pauseRandomizationInterval, final int[] pauseIntervals)
	{
		this(verseRange, numberOfRakaat, reciter, salatRandomizationMode,
				reciteIqama, reciteDhikr, pauseRandomizationInterval);
		setPauseIntervals(pauseIntervals);
	}

	public Prayer(final int verseRange, final int numberOfRakaat,
			final QuranReciter reciter, final int salatRandomizationMode,
			final boolean reciteIqama, final boolean reciteDhikr,
			final int[] pauseIntervals)
	{
		this(verseRange, numberOfRakaat, reciter, salatRandomizationMode,
				reciteIqama, reciteDhikr);
		setPauseIntervals(pauseIntervals);
	}

	public ArrayList<QuranicVerse[]> getAllVerses()
	{
		return verseList;
	}

	public int getCurrentRakatNumber()
	{
		return rakaatPointer + 1;
	}

	public boolean getDhikrRecitation()
	{
		return this.reciteDhikr;
	}

	public boolean getIqamaRecitation()
	{
		return this.reciteIqama;
	}

	public QuranReciter getReciter()
	{
		return reciter;
	}

	public int getPause(int pause)
	{

		int p = MathEssentials.newRandom(pauseRandomizationInterval);
		return Math.max(0,
				MathEssentials.newRandom(1) == 0 ? pauseIntervals[pause] - p
						: pauseIntervals[pause] + p);
	}

	public int getRakaatCount()
	{
		return verseList.size();
	}

	public int getVerseCountForRakat()
	{
		return verseList.get(rakaatPointer).length;
	}

	public QuranicVerse[] getVersesForRakat()
	{
		return verseList.get(rakaatPointer);
	}

	private void initVerseList()
	{
		for (int i = 0; i < numberOfRakaat; i++)
		{ // for every rakat
			int val = 0;
			if (i == 0)
			{
				int base = 0;
				switch (verseRange)
				{
				case (VERSE_RANGE_NARROW):
					base = 5;
					break;
				case (VERSE_RANGE_COMMON):
					base = 10;
					break;
				case (VERSE_RANGE_WIDE):
					base = 15;
					break;
				case (VERSE_RANGE_MOHAMMEDAN):
					base = 25;
					break;
				case (VERSE_RANGE_TRUE_MOHAMMEDAN):
					base = 40;
					break;
				}
				val = base + MathEssentials.newRandom(base / 2);
			}
			else
			{
				val = verseList.get(i - 1).length
						- MathEssentials
								.newRandom(verseList.get(i - 1).length / 2);
			}
			QuranicVerse[] curV = new QuranicVerse[val];
			for (int j = 0; j < curV.length; j++)
			{
				switch (salatRandomizationMode)
				{
				case (Prayer.SALAT_RANDOMIZATION_FULL):
				// randomize every verse
				{
					curV[j] = Quran.getRandomVerse();
					while (curV[j].getSectionNumber() == 1)
						curV[j] = Quran.getRandomVerse();
					break;
				}

				case (Prayer.SALAT_RANDOMIZATION_SINGLE):
					// pick a random verse and continue onward
					if (j != 0) curV[j] = curV[j - 1].getNextVerse();
					else
					{
						curV[j] = Quran.getRandomVerse();
						while (curV[j].getSectionNumber() == 1)
							curV[j] = Quran.getRandomVerse();
					}
					break;

				case (Prayer.SALAT_RANDOMIZATION_IN_SECTION):
					// every verse in a rakaat is from one section only
					if (j != 0)
					{
						curV[j] = curV[j - 1].getNextVerse();
					}
					else
					{
						curV[j] = Quran.getRandomVerse();
						while ((Quran.getInSectionVerseCount(curV[j])
								- curV[j].getVerseNumber() + 1 < curV.length)
								|| curV[j].getSectionNumber() == 1)
							curV[j] = Quran.getRandomVerse();
					}
					break;
				}
			}
			this.verseList.add(curV);
		}
	}

	public void next()
	{
		rakaatPointer++;
	}

	public void previous()
	{
		rakaatPointer--;
	}

	public void rewind()
	{
		rakaatPointer = 0;
	}

	public void setDhikrRecitation(boolean val)
	{
		this.reciteDhikr = val;
	}

	public void setIqamaRecitation(boolean val)
	{
		this.reciteIqama = val;
	}

	public void setPauseIntervals(int[] intervals)
	{
		this.pauseIntervals = intervals;
		this.pauseRandomizationInterval = MathEssentials.getMinValue(intervals) / 2;
	}

	public void setPauseRandomizationInterval(int pause)
	{
		this.pauseRandomizationInterval = pause;
	}

}
