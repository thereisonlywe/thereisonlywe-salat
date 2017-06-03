package info.thereisonlywe.salat.recitation;

import info.thereisonlywe.core.essentials.IOEssentialsAndroid;
import info.thereisonlywe.core.essentials.MathEssentials;
import java.io.File;
import java.io.IOException;

public class RecitationConstants
{
	public static final String RECITATION_PATH = IOEssentialsAndroid.PATH_EXTERNAL_STORAGE
		+ "/thereisonlywe/audio";
	public static final String IMAM_PATH = "imam";
	public static final String FILE_TYPE = ".mp3";
	public static final String ATHAN_ASR_PATH = "athanasr";
	public static final String ATHAN_DHUHR_PATH = "athandhuhr";
	public static final String ATHAN_FAJR_PATH = "athanfajr";
	public static final String ATHAN_ISHA_PATH = "athanisha";
	public static final String ATHAN_MAGHRIB_PATH = "athanmaghrib";
	public static final String IQAMA_PATH = "iqama";
	public static final String DHIKR_PATH = "dhikr";
	public static final String SALAM_PATH = "salam";
	public static final String SAMIALLAH_PATH = "samiallah";
	public static final String TAKBEER_PATH = "takbeer";
	public static final String ABDULKADİR_ŞEHİTOĞLU_PATH = "sehitoglu";
	public static final String İLHAN_TOK_PATH = "tok";
	public static final String AHMET_ŞAHİN_PATH = "sahin";
	public static final String FATİH_KOCA_PATH = "koca";
	public static final String İSMAİL_COŞAR_PATH = "cosar";
	static
	{
		File f = new File(IOEssentialsAndroid.PATH_EXTERNAL_STORAGE
			+ "/thereisonlywe/.nomedia");
		if (!f.exists())
		{
			try
			{
				f.createNewFile();
			}
			catch (IOException e)
			{
			}
		}
		f = getAthanFile(ATHAN_FAJR_PATH);
		if (f.exists()) f.renameTo(getAthanFile(İLHAN_TOK_PATH, ATHAN_FAJR_PATH));
		f = getAthanFile(ATHAN_ASR_PATH);
		if (f.exists()) f.renameTo(getAthanFile(İLHAN_TOK_PATH, ATHAN_ASR_PATH));
		f = getAthanFile(ATHAN_DHUHR_PATH);
		if (f.exists()) f.renameTo(getAthanFile(İLHAN_TOK_PATH, ATHAN_DHUHR_PATH));
		f = getAthanFile(ATHAN_MAGHRIB_PATH);
		if (f.exists()) f
			.renameTo(getAthanFile(İLHAN_TOK_PATH, ATHAN_MAGHRIB_PATH));
		f = getAthanFile(ATHAN_ISHA_PATH);
		if (f.exists()) f.renameTo(getAthanFile(İLHAN_TOK_PATH, ATHAN_ISHA_PATH));
	}

	// For compatibility with older versions
	private static File getAthanFile(String athanPath)
	{
		return new File(RecitationConstants.RECITATION_PATH + "/"
			+ RecitationConstants.IMAM_PATH + "/" + athanPath
			+ RecitationConstants.FILE_TYPE);
	}

	public static String getRandomReciterPath()
	{
		switch (MathEssentials.newRandom(4))
		{
			case (0):
				return İLHAN_TOK_PATH;
			case (1):
				return AHMET_ŞAHİN_PATH;
			case (2):
				return ABDULKADİR_ŞEHİTOĞLU_PATH;
			case (3):
				return FATİH_KOCA_PATH;
			case (4):
				return İSMAİL_COŞAR_PATH;
			default:
				return İLHAN_TOK_PATH;
		}
	}

	public static File getAthanFile(String reciterPath, String athanPath)
	{
		if (!reciterPath.equals(İLHAN_TOK_PATH)
			&& !reciterPath.equals(ABDULKADİR_ŞEHİTOĞLU_PATH)
			&& !reciterPath.equals(AHMET_ŞAHİN_PATH)
			&& !reciterPath.equals(FATİH_KOCA_PATH)
			&& !reciterPath.equals(İSMAİL_COŞAR_PATH)) reciterPath = getRandomReciterPath();
		return new File(RecitationConstants.RECITATION_PATH + "/"
			+ RecitationConstants.IMAM_PATH + "/" + reciterPath + "/" + athanPath
			+ RecitationConstants.FILE_TYPE);
	}

	public static File getExistingAthanFile(String athanPath)
	{
		File f;
		f = getAthanFile(athanPath);
		if (f.exists()) return f;
		f = getAthanFile(İLHAN_TOK_PATH, athanPath);
		if (f.exists()) return f;
		f = getAthanFile(AHMET_ŞAHİN_PATH, athanPath);
		if (f.exists()) return f;
		f = getAthanFile(ABDULKADİR_ŞEHİTOĞLU_PATH, athanPath);
		if (f.exists()) return f;
		f = getAthanFile(FATİH_KOCA_PATH, athanPath);
		if (f.exists()) return f;
		f = getAthanFile(İSMAİL_COŞAR_PATH, athanPath);
		if (f.exists()) return f;
		return null;
	}
}
