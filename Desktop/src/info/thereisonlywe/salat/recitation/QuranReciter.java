package info.thereisonlywe.salat.recitation;

import info.thereisonlywe.core.essentials.IOEssentials;
import info.thereisonlywe.quran.QuranicVerse;
import info.thereisonlywe.quran.text.QuranicTextIdentifier;
import java.io.File;
import java.net.URL;

public class QuranReciter
{
	private final String name;
	private final String path;
	private final boolean isTextReciter;
	private final boolean hasAudhubillah;
	private final boolean hasBasmala;
	private final QuranicTextIdentifier recitedText;

	@SuppressWarnings("unused")
	private QuranReciter()
	{
		name = null;
		path = null;
		isTextReciter = false;
		recitedText = null;
		hasAudhubillah = false;
		hasBasmala = false;
	}

	protected QuranReciter(String name, String path, boolean audhubillah,
		boolean basmala)
	{
		this.name = name;
		this.path = path;
		this.isTextReciter = false;
		this.recitedText = null;
		this.hasAudhubillah = audhubillah;
		this.hasBasmala = basmala;
	}

	protected QuranReciter(String name, String path, boolean audhubillah,
		boolean basmala, QuranicTextIdentifier recitedText)
	{
		this.name = name;
		this.path = path;
		this.isTextReciter = true;
		this.recitedText = recitedText;
		this.hasAudhubillah = audhubillah;
		this.hasBasmala = basmala;
	}

	public QuranicTextIdentifier getRecitedText()
	{
		return recitedText;
	}

	public boolean hasAudhubillah()
	{
		return hasAudhubillah;
	}

	public boolean hasBasmala()
	{
		return hasBasmala;
	}

	public boolean isTextReciter()
	{
		return isTextReciter;
	}

	public String getName()
	{
		return name;
	}

	public String getPath()
	{
		return path;
	}

	public URL getAddress(QuranicVerse v)
	{
		return IOEssentials.toURL("http://QuranServer.thereisonlywe.info/audio/"
			+ path + "/" + v.toIDString() + RecitationConstants.FILE_TYPE);
	}

	public File getFile(QuranicVerse v)
	{
		return new File(RecitationConstants.RECITATION_PATH + File.separator + path
			+ File.separator + v.toIDString() + RecitationConstants.FILE_TYPE);
	}

	@Override
	public String toString()
	{
		return name;
	}
}
