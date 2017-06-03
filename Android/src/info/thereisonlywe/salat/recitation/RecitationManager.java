package info.thereisonlywe.salat.recitation;

import info.thereisonlywe.core.essentials.IOEssentials;
import info.thereisonlywe.quran.Quran;
import info.thereisonlywe.quran.QuranicVerse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class RecitationManager
{
	private static Context context = null;
	private static ComponentName remoteControlReceiver = null;

	public static void setContext(Context c)
	{
		context = c;
	}

	public static Context getContext()
	{
		return context;
	}

	public static void setRemoteControlReceiver(ComponentName cn)
	{
		remoteControlReceiver = cn;
	}

	public static ComponentName getRemoteControlReceiver()
	{
		return remoteControlReceiver;
	}

	public static boolean checkAll(final QuranReciter reciter,
		final QuranicVerse[] verses)
	{
		for (int i = 0; i < verses.length; i++)
		{
			if (!checkAudioFile(reciter, verses[i])) return false;
		}
		return true;
	}

	public static boolean updateAll(final QuranReciter reciter,
		final QuranicVerse[] verses, int updatePolicy)
	{
		for (int i = 0; i < verses.length; i++)
		{
			if (!updateAudioFile(reciter, verses[i], updatePolicy)) return false;
		}
		return true;
	}

	public static boolean checkAudioFile(QuranReciter reciter, QuranicVerse verse)
	{
		return updateAudioFile(reciter, verse,
			IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE);
	}

	public static boolean updateAudioFile(QuranReciter reciter,
		QuranicVerse verse, int updatePolicy)
	{
		if (verse == null || reciter == null) return false;
		else if (!reciter.hasBasmala() && verse.equals(Quran.getVerse(0))) return true;
		else if (!reciter.hasAudhubillah() && verse.equals(Quran.AUDHUBILLAH)) return true;
		File f = reciter.getFile(verse);
		boolean val = IOEssentials.update(f, reciter.getAddress(verse),
			updatePolicy);
		if (val)
		{
			fixTags(reciter, verse);
		}
		return val;
	}

	public static void checkAudioFileUndedicated(final QuranReciter reciter,
		final QuranicVerse verse)
	{
		Thread download = new Thread()
		{
			@Override
			public void run()
			{
				checkAudioFile(reciter, verse);
			}
		};
		new Thread(download).start();
	}

	public static void updateAudioFileUndedicated(final QuranReciter reciter,
		final QuranicVerse verse, final int updatePolicy)
	{
		Thread download = new Thread()
		{
			@Override
			public void run()
			{
				updateAudioFile(reciter, verse, updatePolicy);
			}
		};
		new Thread(download).start();
	}

	public static void updateAllUndedicated(final QuranReciter reciter,
		final QuranicVerse[] verses, final int updatePolicy)
	{
		Thread download = new Thread()
		{
			@Override
			public void run()
			{
				for (int i = 0; i < verses.length; i++)
				{
					if (!updateAudioFile(reciter, verses[i], updatePolicy)) return;
				}
			}
		};
		new Thread(download).start();
	}

	public static void checkAllUndedicated(final QuranReciter reciter,
		final QuranicVerse[] verses)
	{
		Thread download = new Thread()
		{
			@Override
			public void run()
			{
				for (int i = 0; i < verses.length; i++)
				{
					if (!checkAudioFile(reciter, verses[i])) return;
				}
			}
		};
		new Thread(download).start();
	}

	public static void fixTags()
	{
		QuranReciter[] reciters = QuranReciterList.getReciters();
		for (int i = 0; i < reciters.length; i++)
		{
			File f = new File(RecitationConstants.RECITATION_PATH + "/"
				+ reciters[i].getPath());
			if (f.exists())
			{
				File files[] = f.listFiles();
				for (int j = 0; j < files.length; j++)
				{
					int index = files[j].getName().indexOf(".mp3");
					fixTags(
						reciters[i],
						Quran.getVerse(new String(files[j].getName().substring(index - 6,
							index))));
				}
			}
		}
	}

	public static void fixTags(final QuranReciter reciter,
		final QuranicVerse verse)
	{
		File src = reciter.getFile(verse);
		MusicMetadataSet src_set = null;
		try
		{
			src_set = new MyID3().read(src);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
			return;
		}
		if (src_set != null)
		{
			IMusicMetadata tmp = src_set.getSimplified();
			if (tmp != null && tmp.getSongTitle() != null
				&& tmp.getSongTitle().equals("Quran " + verse.toReadableIDString())) return;
			MusicMetadata meta = new MusicMetadata(verse.toNotationString() + " - "
				+ reciter.getName());
			meta.setComposer(reciter.getName());
			meta.setGenre("Quran");
			meta.setProducer("thereisonlywe");
			meta.setSongTitle("Quran " + verse.toReadableIDString());
			meta.setArtist(reciter.getName());
			meta.setAlbum("Quran - " + reciter.getName());
			try
			{
				new MyID3().update(src, src_set, meta);
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
				return;
			}
			catch (ID3WriteException e)
			{
				e.printStackTrace();
				return;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return;
			}
		}
		if (context != null) context.sendBroadcast(new Intent(
			Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(src)));
	}
}
