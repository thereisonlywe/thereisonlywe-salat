package info.thereisonlywe.salat.recitation;

import info.thereisonlywe.quran.text.QuranicTextIdentifier;
import info.thereisonlywe.quran.text.QuranicTextType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class QuranReciterList
{
	public static final QuranReciter AFASY;
	public static final QuranReciter SHURAIM;
	public static final QuranReciter GHAMDI;
	public static final QuranReciter RIFAI;
	public static final QuranReciter BASFAR;
	public static final QuranReciter HUSARY;
	public static final QuranReciter SHATREE;
	public static final QuranReciter WALK;
	public static final QuranReciter BALAYEV;
	public static final QuranReciter MUAIQLY;
	// public static final QuranReciter DUSSARY;
	// public static final QuranReciter JUHAYNEE;
	public static final QuranReciter KHAN;
	// public static final QuranReciter KABIRI;
	public static final QuranReciter HEDAYATFAR;
	public static final QuranReciter LECLERC;
	static
	{
		AFASY = new QuranReciter("Mishary Al-Afasy", "afasy", false, true);
		SHURAIM = new QuranReciter("Saud Al-Shuraim", "shuraim", false, false);
		GHAMDI = new QuranReciter("Saad Al-Ghamdi", "ghamdi", false, true);
		RIFAI = new QuranReciter("Hani Rifai", "rifai", false, false);
		BASFAR = new QuranReciter("Abdullah Basfar", "basfar", false, true);
		HUSARY = new QuranReciter("Husary", "husary", false, true);
		SHATREE = new QuranReciter("Abubakr Ash-Shaatree", "shatree", false, true);
		MUAIQLY = new QuranReciter("Maher Al-Muaiqly", "muaiqly", false, true);
		// DUSSARY = new QuranReciter("Yasser Al-Dosari", "dussary", false, true,
		// "http://everyayah.com/data/Yasser_Ad-Dussary_128kbps/");
		// JUHAYNEE = new QuranReciter("Abdullah Awwad Al-Juhaynee", "juhaynee",
		// false, true,
		// "http://everyayah.com/data/Abdullaah_3awwaad_Al-Juhaynee_128kbps/");
		WALK = new QuranReciter("Ibrahim Walk", "walk", false, true,
			new QuranicTextIdentifier(78, "en", "Saheeh International",
				QuranicTextType.TRANSLATION_TEXT));
		BALAYEV = new QuranReciter("Rasim Balayev", "balayev", false, true,
			new QuranicTextIdentifier(3, "az", "Alikhan Musayev",
				QuranicTextType.TRANSLATION_TEXT));
		KHAN = new QuranReciter("Shamshad Ali Khan", "khan", false, true,
			new QuranicTextIdentifier(-1, "ur", "Maulana Fateh Muhammad Jalundhri",
				QuranicTextType.TRANSLATION_TEXT));
		// KABIRI = new QuranReciter("Kabiri", "kabiri", false, true, new
		// QuranicTextIdentifier(46, "fa",
		// "Naser Makarem Shirazi", QuranicTextType.TRANSLATION_TEXT),
		// "http://everyayah.com/data/translations/Makarem_Kabiri_16Kbps/");
		HEDAYATFAR = new QuranReciter("Hedayatfar", "hedayatfar", false, true,
			new QuranicTextIdentifier(105, "fa", "Mohammad Mahdi Fooladvand",
				QuranicTextType.TRANSLATION_TEXT));
		LECLERC = new QuranReciter("Yusuf Leclerc", "hedayatfar", false, true,
			new QuranicTextIdentifier(29, "fr", "Hamidullah",
				QuranicTextType.TRANSLATION_TEXT));
	}

	public static QuranReciter getReciterByName(String name)
	{
		if (name.equals("")) return null;
		QuranReciter[] reciters = getReciters();
		for (int i = 0; i < reciters.length; i++)
		{
			if (name.equals(reciters[i].getName())) { return reciters[i]; }
		}
		return null;
	}

	public static QuranReciter getReciterByPath(String path)
	{
		if (path.equals("")) return null;
		QuranReciter[] reciters = getReciters();
		for (int i = 0; i < reciters.length; i++)
		{
			if (path.equals(reciters[i].getPath())) { return reciters[i]; }
		}
		return null;
	}

	public static String[] getQuranReciterNames()
	{
		ArrayList<String> rawResult = new ArrayList<String>();
		QuranReciter[] reciters = getReciters();
		for (int i = 0; i < reciters.length; i++)
		{
			if (!reciters[i].isTextReciter())
			{
				rawResult.add(reciters[i].getName());
			}
		}
		String[] result = new String[rawResult.size()];
		return rawResult.toArray(result);
	}

	public static String[] getReciterNames()
	{
		QuranReciter[] reciters = getReciters();
		String[] reciterNames = new String[reciters.length];
		for (int i = 0; i < reciters.length; i++)
		{
			reciterNames[i] = reciters[i].getName();
		}
		return reciterNames;
	}

	public static String[] getTextReciterNames()
	{
		ArrayList<String> rawResult = new ArrayList<String>();
		QuranReciter[] reciters = getReciters();
		for (int i = 0; i < reciters.length; i++)
		{
			if (reciters[i].isTextReciter())
			{
				rawResult.add(reciters[i].getName());
			}
		}
		String[] result = new String[rawResult.size()];
		return rawResult.toArray(result);
	}

	// public static String[] getTextReciterSpecifications()
	// {
	// ArrayList<String> rawResult = new ArrayList<String>();
	// Reciter[] reciters = getReciters();
	// for (int i = 0; i < reciters.length; i++)
	// {
	// if (reciters[i].isTextReciter())
	// {
	// rawResult.add(reciters[i].getRecitedText().getSpecifications());
	// }
	// }
	// String[] result = new String[rawResult.size()];
	// return rawResult.toArray(result);
	// }
	public static QuranReciter[] getReciters()
	{
		QuranReciter[] reciters = new QuranReciter[] { AFASY, BASFAR, HUSARY,
			RIFAI, SHATREE, GHAMDI, SHURAIM, WALK, BALAYEV, MUAIQLY, KHAN,
			HEDAYATFAR, LECLERC };
		Arrays.sort(reciters, new Comparator<QuranReciter>()
		{
			@Override
			public int compare(QuranReciter r1, QuranReciter r2)
			{
				return r1.toString().compareTo(r2.toString());
			}
		});
		return reciters;
	}

	public static String[] getReciterPaths()
	{
		QuranReciter[] reciters = getReciters();
		String[] paths = new String[reciters.length];
		for (int i = 0; i < reciters.length; i++)
		{
			paths[i] = reciters[i].getPath();
		}
		return paths;
	}

	public static int getReciterCount()
	{
		return getReciters().length;
	}
}
