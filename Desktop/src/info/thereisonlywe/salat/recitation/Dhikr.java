package info.thereisonlywe.salat.recitation;

import info.thereisonlywe.quran.QuranicVerse;

/**
 * 
 * @author thereisonlywe
 * @since October 28th 2011
 */
public class Dhikr {

	private QuranicVerse[] verseList;
	private int pointer;

	@SuppressWarnings("unused")
	private Dhikr()
	{
	}

	public Dhikr(final QuranicVerse verse)
	{
		QuranicVerse[] verseList = new QuranicVerse[] { verse };
		this.verseList = verseList;
		this.pointer = 0;
	}

	public Dhikr(final QuranicVerse verseStart, int length) // add consecutive
	{
		QuranicVerse[] verseList = new QuranicVerse[length];
		verseList[0] = verseStart;
		for (int i = 1; i < length; i++)
		{
			verseList[i] = verseList[i - 1].getNextVerse();
		}
		this.verseList = verseList;
		this.pointer = 0;
	}

	public Dhikr(final QuranicVerse[] verseList)
	{
		this.verseList = verseList;
		this.pointer = 0;
	}

	public Dhikr(final QuranicVerse[] verseList, final int pointer)
	{
		this.verseList = verseList;
		this.pointer = pointer;
	}

	public QuranicVerse[] getAllVerses()
	{
		return verseList;
	}

	public int getVerseCount()
	{
		return verseList.length;
	}

	public QuranicVerse[] getRemainingVerses()
	{
		QuranicVerse[] result = new QuranicVerse[verseList.length - pointer];
		System.arraycopy(verseList, pointer, result, 0, result.length);
		return result;
	}

	public int getNumberOfVersesRemaining()
	{
		return verseList.length - pointer;
	}

	public QuranicVerse getCurrentVerse()
	{
		if (pointer >= 0 && pointer < verseList.length) return verseList[pointer];
		else return null;
	}

	public void next()
	{
		pointer++;
	}

	public void previous()
	{
		pointer--;
		if (pointer < 0) pointer = 0;
	}

	public QuranicVerse getNextVerse()
	{
		return getNextNthVerse(1);
	}

	public QuranicVerse getNextNthVerse(int n)
	{
		if (pointer + n >= 0 && pointer + n < verseList.length) return verseList[pointer
				+ n];
		else return null;
	}

	public QuranicVerse getPreviousNthVerse(int n)
	{
		if (pointer - n >= 0 && pointer - n < verseList.length) return verseList[pointer
				- n];
		else return null;
	}

	public QuranicVerse getPreviousVerse()
	{
		return getPreviousNthVerse(1);
	}

	public void rewind()
	{
		pointer = 0;
	}

}
