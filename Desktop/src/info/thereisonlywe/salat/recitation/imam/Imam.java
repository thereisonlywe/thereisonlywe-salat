package info.thereisonlywe.salat.recitation.imam;

import info.thereisonlywe.core.audio.AudioPlayer;
import info.thereisonlywe.core.essentials.IOEssentials;
import info.thereisonlywe.core.essentials.SystemEssentials;
import info.thereisonlywe.quran.Quran;
import info.thereisonlywe.quran.QuranicVerse;
import info.thereisonlywe.salat.recitation.QuranReciter;
import info.thereisonlywe.salat.recitation.QuranReciterList;
import info.thereisonlywe.salat.recitation.RecitationConstants;
import info.thereisonlywe.salat.recitation.RecitationManager;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class Imam
{
	public static final File TAKBEER = new File(
		RecitationConstants.RECITATION_PATH + File.separator
			+ RecitationConstants.IMAM_PATH + File.separator
			+ RecitationConstants.TAKBEER_PATH + RecitationConstants.FILE_TYPE);
	public static final File SALAM = new File(RecitationConstants.RECITATION_PATH
		+ File.separator + RecitationConstants.IMAM_PATH + File.separator
		+ RecitationConstants.SALAM_PATH + RecitationConstants.FILE_TYPE);
	public static final File SAMIALLAH = new File(
		RecitationConstants.RECITATION_PATH + File.separator
			+ RecitationConstants.IMAM_PATH + File.separator
			+ RecitationConstants.SAMIALLAH_PATH + RecitationConstants.FILE_TYPE);
	public static final File IQAMA = new File(RecitationConstants.RECITATION_PATH
		+ File.separator + RecitationConstants.IMAM_PATH + File.separator
		+ RecitationConstants.IQAMA_PATH + RecitationConstants.FILE_TYPE);
	public static final File DHIKR = new File(RecitationConstants.RECITATION_PATH
		+ File.separator + RecitationConstants.IMAM_PATH + File.separator
		+ RecitationConstants.DHIKR_PATH + RecitationConstants.FILE_TYPE);
	public static final int STATUS_FRESH = 0;
	public static final int STATUS_RECITING = 1;
	public static final int STATUS_WAITING_FOR_PROSTRATION = 2;
	public static final int STATUS_FILE_ERROR = 3;
	public static final int STATUS_INITIALIZING = 4;
	public static final int STATUS_COMPLETE = 5;
	protected boolean reciterSwitchAllowed = true;
	protected boolean textReciterSwitchAllowed = false;
	protected boolean requireTextRecitation = false;
	protected volatile boolean ongoingDownload = false;
	protected boolean loop = false;
	protected double volume = 0.5;
	protected volatile boolean alive = false; // alive = recitation is in
	// progress
	protected volatile boolean sigTerm = false;
	protected volatile boolean sigPause = false;
	protected AudioPlayer player = new AudioPlayer();
	protected QuranReciter reciter;
	protected QuranReciter textReciter;
	protected QuranicVerse currentVerse = null;
	private File recitationFile;
	protected static final ArrayList<Imam> aliveImams = new ArrayList<Imam>();
	protected int status = STATUS_FRESH;
	// public void setPan(double v) {
	// pan = v;
	// }
	//
	// public void setPan(int v) {
	// pan = v * 1.0 / 100.0;
	// }
	protected boolean forceUpdates = false;

	public int getStatus()
	{
		return status;
	}

	public static ArrayList<Imam> getAliveImams()
	{
		return aliveImams;
	}

	public static Imam getAliveImam()
	{
		return aliveImams.size() > 0 ? aliveImams.get(0) : null;
	}

	public void proceed()
	{
		status = STATUS_RECITING;
	}

	public Imam(final File f)
	{
		recitationFile = f;
		sigTerm = false;
		sigPause = false;
		status = STATUS_FRESH;
	}

	protected Imam()
	{
		recitationFile = null;
	}

	public void begin()
	{
		alive = true;
		aliveImams.add(this);
		status = STATUS_INITIALIZING;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				if (recitationFile == null)
				{
					endSession();
					return;
				}
				if (!recitationFile.exists() || forceUpdates)
				{
					if (recitationFile.getPath().contains(
						RecitationConstants.ATHAN_FAJR_PATH)
						&& !IOEssentials.update(
							recitationFile,
							getAthanFileURL(recitationFile,
								RecitationConstants.ATHAN_FAJR_PATH),
							forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
								: IOEssentials.FILE_UPDATE_POLICY_NEVER))
					{
						File f = RecitationConstants
							.getExistingAthanFile(RecitationConstants.ATHAN_FAJR_PATH);
						if (f == null)
						{
							endSession();
							return;
						}
						else recitationFile = f;
					}
					else if (recitationFile.getPath().contains(
						RecitationConstants.ATHAN_DHUHR_PATH)
						&& !IOEssentials.update(
							recitationFile,
							getAthanFileURL(recitationFile,
								RecitationConstants.ATHAN_DHUHR_PATH),
							forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
								: IOEssentials.FILE_UPDATE_POLICY_NEVER))
					{
						File f = RecitationConstants
							.getExistingAthanFile(RecitationConstants.ATHAN_DHUHR_PATH);
						if (f == null)
						{
							endSession();
							return;
						}
						else recitationFile = f;
					}
					else if (recitationFile.getPath().contains(
						RecitationConstants.ATHAN_ASR_PATH)
						&& !IOEssentials.update(
							recitationFile,
							getAthanFileURL(recitationFile,
								RecitationConstants.ATHAN_ASR_PATH),
							forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
								: IOEssentials.FILE_UPDATE_POLICY_NEVER))
					{
						File f = RecitationConstants
							.getExistingAthanFile(RecitationConstants.ATHAN_ASR_PATH);
						if (f == null)
						{
							endSession();
							return;
						}
						else recitationFile = f;
					}
					else if (recitationFile.getPath().contains(
						RecitationConstants.ATHAN_MAGHRIB_PATH)
						&& !IOEssentials.update(
							recitationFile,
							getAthanFileURL(recitationFile,
								RecitationConstants.ATHAN_MAGHRIB_PATH),
							forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
								: IOEssentials.FILE_UPDATE_POLICY_NEVER))
					{
						File f = RecitationConstants
							.getExistingAthanFile(RecitationConstants.ATHAN_MAGHRIB_PATH);
						if (f == null)
						{
							endSession();
							return;
						}
						else recitationFile = f;
					}
					else if (recitationFile.getPath().contains(
						RecitationConstants.ATHAN_ISHA_PATH)
						&& !IOEssentials.update(
							recitationFile,
							getAthanFileURL(recitationFile,
								RecitationConstants.ATHAN_ISHA_PATH),
							forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
								: IOEssentials.FILE_UPDATE_POLICY_NEVER))
					{
						File f = RecitationConstants
							.getExistingAthanFile(RecitationConstants.ATHAN_ISHA_PATH);
						if (f == null)
						{
							endSession();
							return;
						}
						else recitationFile = f;
					}
					else if (recitationFile.equals(DHIKR)
						&& !IOEssentials.update(DHIKR,
							getPrayerFileURL(RecitationConstants.DHIKR_PATH),
							forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
								: IOEssentials.FILE_UPDATE_POLICY_NEVER))
					{
						endSession();
						return;
					}
					else if (recitationFile.equals(IQAMA)
						&& !IOEssentials.update(IQAMA,
							getPrayerFileURL(RecitationConstants.IQAMA_PATH),
							forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
								: IOEssentials.FILE_UPDATE_POLICY_NEVER))
					{
						endSession();
						return;
					}
					else if (recitationFile.equals(TAKBEER)
						&& !IOEssentials.update(TAKBEER,
							getPrayerFileURL(RecitationConstants.TAKBEER_PATH),
							forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
								: IOEssentials.FILE_UPDATE_POLICY_NEVER))
					{
						endSession();
						return;
					}
					else if (recitationFile.equals(SALAM)
						&& !IOEssentials.update(SALAM,
							getPrayerFileURL(RecitationConstants.SALAM_PATH),
							forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
								: IOEssentials.FILE_UPDATE_POLICY_NEVER))
					{
						endSession();
						return;
					}
					else if (recitationFile.equals(SAMIALLAH)
						&& !IOEssentials.update(SAMIALLAH,
							getPrayerFileURL(RecitationConstants.SAMIALLAH_PATH),
							forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
								: IOEssentials.FILE_UPDATE_POLICY_NEVER))
					{
						endSession();
						return;
					}
				}
				status = STATUS_RECITING;
				recite(recitationFile);
				endSession();
			}
		}).start();
	}

	public void setForceUpdates(boolean val)
	{
		forceUpdates = val;
	}

	public QuranReciter getReciter()
	{
		return reciter;
	}

	public QuranReciter getTextReciter()
	{
		return textReciter;
	}

	public QuranicVerse getVerse()
	{
		return currentVerse;
	}

	public boolean isAlive()
	{
		return alive;
	}

	// protected double pan = 0.0;
	public boolean isPaused()
	{
		return sigPause;
	}

	public void revive()
	{
		if (!sigTerm)
		{
			sigPause = false;
		}
	}

	public void setLoop(boolean val)
	{
		loop = val;
	}

	public boolean getLoop()
	{
		return loop;
	}

	public void setReciterSwitchAllowed(boolean val)
	{
		reciterSwitchAllowed = val;
	}

	public void setRequireTextRecitation(boolean val)
	{
		requireTextRecitation = val;
	}

	public void setTextReciterSwitchAllowed(boolean val)
	{
		textReciterSwitchAllowed = val;
	}

	public void setVolume(double v)
	{
		volume = v;
	}

	public void setVolume(int v)
	{
		volume = v * 1.0 / 100.0;
	}

	public void terminate()
	{
		if (sigTerm || !isAlive())
		{
			return;
		}
		else if (sigPause)
		{
			sigTerm = true;
			sigPause = false;
			alive = false;
		}
		else
		{
			sigPause = true;
		}
	}

	protected boolean checkAthanFiles()
	{
		if (!IOEssentials.update(TAKBEER,
			getPrayerFileURL(RecitationConstants.ATHAN_FAJR_PATH),
			forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
				: IOEssentials.FILE_UPDATE_POLICY_NEVER)) { return false; }
		if (!IOEssentials.update(SALAM,
			getPrayerFileURL(RecitationConstants.SALAM_PATH),
			forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
				: IOEssentials.FILE_UPDATE_POLICY_NEVER)) { return false; }
		if (!IOEssentials.update(SAMIALLAH,
			getPrayerFileURL(RecitationConstants.SAMIALLAH_PATH),
			forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
				: IOEssentials.FILE_UPDATE_POLICY_NEVER)) { return false; }
		return true;
	}

	public boolean checkOptionalPrayerFiles()
	{
		if (!IOEssentials.update(DHIKR,
			getPrayerFileURL(RecitationConstants.DHIKR_PATH),
			forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
				: IOEssentials.FILE_UPDATE_POLICY_NEVER)) { return false; }
		if (!IOEssentials.update(IQAMA,
			getPrayerFileURL(RecitationConstants.IQAMA_PATH),
			forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
				: IOEssentials.FILE_UPDATE_POLICY_NEVER)) { return false; }
		return true;
	}

	public boolean checkRequiredPrayerFiles()
	{
		if (sigTerm) { return false; }
		if (!IOEssentials.update(TAKBEER,
			getPrayerFileURL(RecitationConstants.TAKBEER_PATH),
			forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
				: IOEssentials.FILE_UPDATE_POLICY_NEVER)) { return false; }
		if (!IOEssentials.update(SALAM,
			getPrayerFileURL(RecitationConstants.SALAM_PATH),
			forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
				: IOEssentials.FILE_UPDATE_POLICY_NEVER)) { return false; }
		if (!IOEssentials.update(SAMIALLAH,
			getPrayerFileURL(RecitationConstants.SAMIALLAH_PATH),
			forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
				: IOEssentials.FILE_UPDATE_POLICY_NEVER)) { return false; }
		return true;
	}

	protected void endSession()
	{
		aliveImams.remove(this);
		player.reset();
		alive = false;
		sigTerm = false;
		sigPause = false;
		ongoingDownload = false;
		status = STATUS_COMPLETE;
	}

	public static URL getAthanFileURL(File f, String athan)
	{
		String s = f.getAbsolutePath();
		if (s.contains(RecitationConstants.İLHAN_TOK_PATH)) return getPrayerFileURL(RecitationConstants.İLHAN_TOK_PATH
			+ "/" + athan);
		if (s.contains(RecitationConstants.ABDULKADİR_ŞEHİTOĞLU_PATH)) return getPrayerFileURL(RecitationConstants.ABDULKADİR_ŞEHİTOĞLU_PATH
			+ "/" + athan);
		if (s.contains(RecitationConstants.FATİH_KOCA_PATH)) return getPrayerFileURL(RecitationConstants.FATİH_KOCA_PATH
			+ "/" + athan);
		if (s.contains(RecitationConstants.İSMAİL_COŞAR_PATH)) return getPrayerFileURL(RecitationConstants.İSMAİL_COŞAR_PATH
			+ "/" + athan);
		if (s.contains(RecitationConstants.AHMET_ŞAHİN_PATH)) return getPrayerFileURL(RecitationConstants.AHMET_ŞAHİN_PATH
			+ "/" + athan);
		return null;
	}

	public static URL getPrayerFileURL(String file)
	{
		return IOEssentials.toURL("http://QuranServer.thereisonlywe.info/audio/"
			+ RecitationConstants.IMAM_PATH + "/" + file
			+ RecitationConstants.FILE_TYPE);
	}

	protected void intelliSleep(long amount)
	{
		int sleepCount = 0;
		if (sigPause)
		{
			while (sigPause)
			{
				SystemEssentials.sleep(10);
			}
		}
		else
		{
			while (sleepCount < amount && !sigTerm)
			{
				SystemEssentials.sleep(10);
				sleepCount += 10;
			}
		}
	}

	protected void recite(File f)
	{
		double vol = volume;
		player.setVolume(volume);
		player.play(f);
		do
		{
			if (sigTerm)
			{
				return;
			}
			else if (sigPause)
			{
				player.pause(f);
				while (sigPause)
				{
					intelliSleep(10);
					if (sigTerm) { return; }
				}
				player.resume(f);
			}
			else
			{
				if (volume != vol)
				{
					vol = volume;
					player.setVolume(volume);
				}
			}
			intelliSleep(10);
		}
		while (player != null && (!player.isComplete(f)));
	}

	protected void reciteAudhubillah(QuranReciter reciter)
	{
		if (!reciter.hasAudhubillah()) { return; }
		final File f = reciter.getFile(Quran.AUDHUBILLAH);
		if (!sigTerm && !f.exists())
		{
			RecitationManager.checkAudioFile(reciter, Quran.AUDHUBILLAH);
		}
		if (!sigTerm && f.exists())
		{
			recite(f);
		}
	}

	protected void reciteBasmala(QuranReciter reciter)
	{
		if (!reciter.hasBasmala()) { return; }
		final File f = reciter.getFile(Quran.getVerse(0));
		if (!sigTerm && !f.exists())
		{
			RecitationManager.checkAudioFile(reciter, Quran.getVerse(0));
		}
		if (!sigTerm && f.exists())
		{
			recite(f);
		}
	}

	protected void reciteCurrentVerse()
	{
		File f = reciter.getFile(currentVerse);
		final boolean fileExists = f.exists();
		if (!fileExists)
		{
			if (sigTerm) return;
			while (ongoingDownload)
				SystemEssentials.sleep(10);
			if (RecitationManager.checkAudioFile(reciter, currentVerse))
			{
				recite(f);
			}
			else
			{
				final QuranReciter current = reciter;
				if (switchReciter())
				{
					recite(reciter.getFile(currentVerse));
					reciter = current; // switch back to original reciter
				}
				else
				{
					status = STATUS_FILE_ERROR;
					while (!RecitationManager.checkAudioFile(reciter, currentVerse)
						&& !sigTerm)
						intelliSleep(IOEssentials.NETWORK_TIMEOUT_SHORT);
					if (sigTerm) return;
					status = STATUS_RECITING;
					recite(f);
				}
			}
		}
		else
		{
			recite(f);
		}
		if (textReciter != null && !sigTerm)
		{
			if (currentVerse.getIndex() == 0 && !textReciter.hasBasmala())
			{
				// pass
			}
			else if (textReciter.getFile(currentVerse).exists())
			{
				recite(textReciter.getFile(currentVerse));
			}
			else
			{
				if (sigTerm) { return; }
				while (ongoingDownload)
					SystemEssentials.sleep(10);
				if (RecitationManager.checkAudioFile(textReciter, currentVerse))
				{
					recite(textReciter.getFile(currentVerse));
				}
				else
				{
					final QuranReciter current = textReciter;
					if (switchTextReciter())
					{
						recite(textReciter.getFile(currentVerse));
						textReciter = current; // switch back to original text
						// reciter
					}
					else if (requireTextRecitation)
					{
						status = STATUS_FILE_ERROR;
						while (!RecitationManager.checkAudioFile(textReciter, currentVerse)
							&& !sigTerm)
							intelliSleep(IOEssentials.NETWORK_TIMEOUT_SHORT);
						if (sigTerm) return;
						status = STATUS_RECITING;
						recite(textReciter.getFile(currentVerse));
					}
					else
					{
						// skip to next verse
					}
				}
			}
		}
		while (ongoingDownload)
			SystemEssentials.sleep(10);
	}

	private boolean switchReciter()
	{
		if (!reciterSwitchAllowed) { return false; }
		final QuranReciter[] reciters = QuranReciterList.getReciters();
		for (final QuranReciter reciter2 : reciters)
		{
			if (sigTerm)
			{
				return false;
			}
			else if (reciter2 == reciter || reciter2.isTextReciter())
			{
				continue;
			}
			if (reciter2.getFile(currentVerse).exists())
			{
				reciter = reciter2;
				return true;
			}
		}
		// if(IOEssentials.gotInternetConnection())
		// {
		// // try downloading the verse from another reciter
		// int i = 0;
		// QuranReciter reciter2 = QuranReciterList.getReciters()[i];
		// while(reciter2 == reciter || reciter2.isTextReciter())
		// {
		// i++;
		// reciter2 = QuranReciterList.getReciters()[i];
		// }
		// if(RecitationManager.updateAudioFile(reciter2, currentVerse,
		// forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE :
		// IOEssentials.FILE_UPDATE_POLICY_NEVER))
		// {
		// reciter = reciter2;
		// return true;
		// }
		// }
		return false;
	}

	private boolean switchTextReciter()
	{
		if (!textReciterSwitchAllowed) { return false; }
		final QuranReciter[] treciters = QuranReciterList.getReciters();
		for (int i = 0; i < treciters.length; i++)
		{
			if (sigTerm)
			{
				return false;
			}
			else if (!treciters[i].isTextReciter() || treciters[i] == textReciter)
			{
				continue;
			}
			if (treciters[i].getFile(currentVerse).exists())
			{
				textReciter = treciters[i];
				return true;
			}
		}
		// if(IOEssentials.gotInternetConnection())
		// {
		// // try downloading the verse from another textreciter
		// int i = 0;
		// QuranReciter reciter2 = QuranReciterList.getReciters()[i];
		// while(reciter2 == textReciter || !reciter2.isTextReciter())
		// {
		// i++;
		// reciter2 = QuranReciterList.getReciters()[i];
		// }
		// if(RecitationManager.updateAudioFile(reciter2, currentVerse,
		// forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE :
		// IOEssentials.FILE_UPDATE_POLICY_NEVER))
		// {
		// textReciter = reciter2;
		// return true;
		// }
		// }
		return false;
	}
}
