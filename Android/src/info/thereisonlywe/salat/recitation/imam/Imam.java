package info.thereisonlywe.salat.recitation.imam;

import info.thereisonlywe.core.essentials.IOEssentials;
import info.thereisonlywe.core.essentials.SystemEssentialsAndroid;
import info.thereisonlywe.quran.Quran;
import info.thereisonlywe.quran.QuranicVerse;
import info.thereisonlywe.salat.recitation.QuranReciter;
import info.thereisonlywe.salat.recitation.QuranReciterList;
import info.thereisonlywe.salat.recitation.RecitationConstants;
import info.thereisonlywe.salat.recitation.RecitationManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import android.Manifest.permission;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.SystemClock;

public class Imam
{
	public static final File TAKBEER = new File(
		RecitationConstants.RECITATION_PATH + "/" + RecitationConstants.IMAM_PATH
			+ "/" + RecitationConstants.TAKBEER_PATH + RecitationConstants.FILE_TYPE);
	public static final File SALAM = new File(RecitationConstants.RECITATION_PATH
		+ "/" + RecitationConstants.IMAM_PATH + "/"
		+ RecitationConstants.SALAM_PATH + RecitationConstants.FILE_TYPE);
	public static final File SAMIALLAH = new File(
		RecitationConstants.RECITATION_PATH + "/" + RecitationConstants.IMAM_PATH
			+ "/" + RecitationConstants.SAMIALLAH_PATH
			+ RecitationConstants.FILE_TYPE);
	public static final File IQAMA = new File(RecitationConstants.RECITATION_PATH
		+ "/" + RecitationConstants.IMAM_PATH + "/"
		+ RecitationConstants.IQAMA_PATH + RecitationConstants.FILE_TYPE);
	public static final File DHIKR = new File(RecitationConstants.RECITATION_PATH
		+ "/" + RecitationConstants.IMAM_PATH + "/"
		+ RecitationConstants.DHIKR_PATH + RecitationConstants.FILE_TYPE);
	// public static final File ATHAN_ASR = new File(
	// RecitationConstants.RECITATION_PATH + "/" + RecitationConstants.IMAM_PATH
	// + "/" + RecitationConstants.ATHAN_ASR_PATH
	// + RecitationConstants.FILE_TYPE);
	// public static final File ATHAN_DHUHR = new File(
	// RecitationConstants.RECITATION_PATH + "/" + RecitationConstants.IMAM_PATH
	// + "/" + RecitationConstants.ATHAN_DHUHR_PATH
	// + RecitationConstants.FILE_TYPE);
	// public static final File ATHAN_FAJR = new File(
	// RecitationConstants.RECITATION_PATH + "/" + RecitationConstants.IMAM_PATH
	// + "/" + RecitationConstants.ATHAN_FAJR_PATH
	// + RecitationConstants.FILE_TYPE);
	// public static final File ATHAN_ISHA = new File(
	// RecitationConstants.RECITATION_PATH + "/" + RecitationConstants.IMAM_PATH
	// + "/" + RecitationConstants.ATHAN_ISHA_PATH
	// + RecitationConstants.FILE_TYPE);
	// public static final File ATHAN_MAGHRIB = new File(
	// RecitationConstants.RECITATION_PATH + "/" + RecitationConstants.IMAM_PATH
	// + "/" + RecitationConstants.ATHAN_MAGHRIB_PATH
	// + RecitationConstants.FILE_TYPE);
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
	protected float volume = 1.0f;
	protected int volumeInt = 100;
	protected volatile boolean alive = false; // alive = recitation is in
	// progress
	protected volatile boolean sigTerm = false;
	protected volatile boolean sigPause = false;
	protected MediaPlayer player = null;
	protected QuranReciter reciter;
	protected QuranReciter textReciter;
	protected QuranicVerse currentVerse = null;
	private File recitationFile;
	protected int status = STATUS_FRESH;
	private volatile boolean recitationComplete = false;
	protected static final ArrayList<Imam> aliveImams = new ArrayList<Imam>();
	protected WifiLock wifiLock = null;
	protected final AudioManager am = RecitationManager.getContext() == null ? null
		: (AudioManager) RecitationManager.getContext().getSystemService(
			Context.AUDIO_SERVICE);
	private OnAudioFocusChangeListener afcl;
	private boolean relentlessAudioFocus = false;
	private boolean gainFocus = false;
	// public void setPan(double v) {
	// pan = v;
	// }
	//
	// public void setPan(int v) {
	// pan = v * 1.0 / 100.0;
	// }
	protected boolean forceUpdates = false;

	public void setRelentlessAudioFocus(boolean val)
	{
		relentlessAudioFocus = val;
	}

	public void setGainFocus(boolean val)
	{
		gainFocus = val;
	}

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

	private void requestAudioFocus()
	{
		if (am == null) return;
		afcl = new OnAudioFocusChangeListener()
		{
			@Override
			public void onAudioFocusChange(int focusChange)
			{
				if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
				{
					if (Imam.this != null && Imam.this.isAlive() && !relentlessAudioFocus)
					{
						Imam.this.terminate();
					}
				}
				else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
				{
				}
				else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
				{
					if (Imam.this != null && Imam.this.isAlive() && !relentlessAudioFocus)
					{
						Imam.this.terminate();
						Imam.this.terminate();
					}
				}
				else if (Imam.this != null && Imam.this.isAlive()
					&& Imam.this.isPaused())
				{
					Imam.this.revive();
				}
			}
		};
		requestAudioFocus(afcl);
	}

	protected void requestAudioFocus(OnAudioFocusChangeListener afcl)
	{
		if (am == null) return;
		this.afcl = afcl;
		int result = am.requestAudioFocus(afcl, AudioManager.STREAM_MUSIC,
			gainFocus ? AudioManager.AUDIOFOCUS_GAIN
				: AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		int counter = 5000;
		while (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED)
		{
			if (sigTerm || counter < 0) return;
			SystemClock.sleep(1000);
			result = am.requestAudioFocus(afcl, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);
			counter -= 1000;
		}
		// this line maybe unnecessary here
		if (am != null && RecitationManager.getRemoteControlReceiver() != null) am
			.registerMediaButtonEventReceiver(RecitationManager
				.getRemoteControlReceiver());
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
					if (RecitationManager.getContext() != null
						&& SystemEssentialsAndroid.isPermissionGranted(
							RecitationManager.getContext(), permission.WAKE_LOCK))
					{
						wifiLock = ((WifiManager) RecitationManager.getContext()
							.getSystemService(Context.WIFI_SERVICE)).createWifiLock(3,
							"thereisonlywe Salat API");
						wifiLock.acquire();
					}
					else
					{
						forceUpdates = false;
					}
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
				requestAudioFocus();
				status = STATUS_RECITING;
				recite(recitationFile);
				endSession();
			}
		}).start();
	}

	public void setForceUpdates(boolean val)
	{
		if (val && wifiLock != null) forceUpdates = val;
		else forceUpdates = false;
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

	// between 0 and 100
	public void setVolume(int v)
	{
		volumeInt = v;
		volume = Math.min(1.0f, (float) (1 - (Math.log(100 - v) / Math.log(100))));
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
		if (player != null) player.reset();
		alive = false;
		sigTerm = false;
		sigPause = false;
		ongoingDownload = false;
		status = STATUS_COMPLETE;
		if (am != null) am.abandonAudioFocus(afcl);
		if (wifiLock != null) wifiLock.release();
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

	public static Uri getPrayerFileUri(String file)
	{
		return Uri.parse("http://QuranServer.thereisonlywe.info/audio/"
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
				SystemClock.sleep(10);
			}
		}
		else
		{
			while (sleepCount < amount && !sigTerm)
			{
				SystemClock.sleep(10);
				sleepCount += 10;
			}
		}
	}

	protected void recite(File f)
	{
		try
		{
			recitationComplete = false;
			double vol = volume;
			player = new MediaPlayer();
			player.setDataSource(f.getAbsolutePath());
			player.setOnCompletionListener(new OnCompletionListener()
			{
				@Override
				public void onCompletion(MediaPlayer mp)
				{
					recitationComplete = true;
				}
			});
			player.setVolume(volume, volume);
			player.prepare();
			player.start();
			do
			{
				if (sigTerm)
				{
					return;
				}
				else if (sigPause)
				{
					player.pause();
					while (sigPause)
					{
						intelliSleep(10);
						if (sigTerm) { return; }
					}
					player.start();
				}
				else
				{
					player.start();
					if (volume != vol)
					{
						vol = volume;
						player.setVolume(volume, volume);
					}
				}
				intelliSleep(10);
			}
			while (!recitationComplete);
			player.reset();
			player.release();
			player = null;
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (!recitationComplete)
			{
				player.reset();
				player.release();
				player = null;
			}
		}
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
				SystemClock.sleep(10);
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
					SystemClock.sleep(10);
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
						// switch back to original text reciter
						textReciter = current;
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
			SystemClock.sleep(10);
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
		return false;
	}
}
