package info.thereisonlywe.salat.recitation.imam;

import info.thereisonlywe.core.essentials.IOEssentials;
import info.thereisonlywe.core.essentials.SystemEssentialsAndroid;
import info.thereisonlywe.quran.QuranicVerse;
import info.thereisonlywe.salat.recitation.Dhikr;
import info.thereisonlywe.salat.recitation.QuranReciter;
import info.thereisonlywe.salat.recitation.RecitationManager;
import android.Manifest.permission;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class DhikrImam extends Imam
{
	protected Dhikr dhikr;
	protected final boolean downloadsFirst;
	private boolean readyForRecitation = false;
	private static DhikrImam imamCandidate;
	private volatile boolean held = false;
	private volatile boolean sigPauseBeforeNext = false;
	private volatile boolean audioFocusLoss = false;
	private volatile boolean audioFocusLossDuck = false;
	private volatile boolean skipProstration = false;
	private TelephonyManager tm = null;
	private final PhoneStateListener psl = new PhoneStateListener()
	{
		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			if (state == TelephonyManager.CALL_STATE_RINGING)
			{
				if (this != null && isAlive()) hold();
			}
			else if (state == TelephonyManager.CALL_STATE_IDLE)
			{
				if (this != null && isAlive() && isHeld()) revive();
			}
			else if (state == TelephonyManager.CALL_STATE_OFFHOOK)
			{
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	};
	private final OnAudioFocusChangeListener afcl = new OnAudioFocusChangeListener()
	{
		@Override
		public void onAudioFocusChange(int focusChange)
		{
			if (held) return;
			else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
			{
				if (DhikrImam.this != null && DhikrImam.this.isAlive())
				{
					audioFocusLossDuck = true;
					// DhikrImam.this.terminate();
					setVolume(volumeInt / 2);
				}
			}
			else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
			{
				if (DhikrImam.this != null && DhikrImam.this.isAlive()
					&& !DhikrImam.this.isPaused())
				{
					audioFocusLoss = true;
					DhikrImam.this.terminate();
				}
			}
			else if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
			{
				if (am != null && RecitationManager.getRemoteControlReceiver() != null) am
					.registerMediaButtonEventReceiver(RecitationManager
						.getRemoteControlReceiver());
				if (DhikrImam.this != null && DhikrImam.this.isAlive())
				{
					if (audioFocusLoss && DhikrImam.this.isPaused()) DhikrImam.this
						.revive();
					else if (audioFocusLossDuck) setVolume(volumeInt * 2);
					audioFocusLoss = false;
					audioFocusLossDuck = false;
				}
			}
			else if (focusChange == AudioManager.AUDIOFOCUS_LOSS)
			{
				if (am != null && RecitationManager.getRemoteControlReceiver() != null) am
					.unregisterMediaButtonEventReceiver(RecitationManager
						.getRemoteControlReceiver());
				if (DhikrImam.this != null && DhikrImam.this.isAlive())
				{
					audioFocusLoss = true;
					DhikrImam.this.terminate();
					// DhikrImam.this.terminate();
				}
			}
		}
	};

	public DhikrImam()
	{
		this(imamCandidate);
		setLoop(imamCandidate.getLoop());
		setForceUpdates(imamCandidate.forceUpdates);
		setReciterSwitchAllowed(imamCandidate.reciterSwitchAllowed);
		setRequireTextRecitation(imamCandidate.requireTextRecitation);
		setTextReciterSwitchAllowed(imamCandidate.textReciterSwitchAllowed);
		setSkipProstration(imamCandidate.skipProstration);
		volume = imamCandidate.volume;
		// this.downloadsFirst = false;
		// dhikr = null;
	}

	private DhikrImam(DhikrImam imam)
	{
		this(imam.getDhikr(), imam.getReciter(), imam.getTextReciter(),
			imam.downloadsFirst);
	}

	public DhikrImam(Dhikr salat, QuranReciter reciter, QuranReciter textReciter,
		boolean downloadsFirst)
	{
		super();
		this.dhikr = salat;
		this.reciter = reciter;
		this.textReciter = textReciter;
		this.downloadsFirst = downloadsFirst;
		this.sigTerm = false;
		this.sigPause = false;
		status = STATUS_FRESH;
	}

	public DhikrImam(Dhikr salat, QuranReciter reciter, QuranReciter textReciter)
	{
		this(salat, reciter, textReciter, false);
	}

	public DhikrImam(Dhikr salat, QuranReciter reciter)
	{
		this(salat, reciter, null);
	}

	public DhikrImam(Dhikr salat, QuranReciter reciter, boolean downloadsFirst)
	{
		this(salat, reciter, null, downloadsFirst);
	}

	public DhikrImam(QuranicVerse[] salat, QuranReciter reciter,
		QuranReciter textReciter, boolean downloadsFirst)
	{
		this(new Dhikr(salat), reciter, textReciter, downloadsFirst);
	}

	public DhikrImam(QuranicVerse[] salat, QuranReciter reciter,
		boolean downloadsFirst)
	{
		this(new Dhikr(salat), reciter, null, downloadsFirst);
	}

	public DhikrImam(QuranicVerse[] salat, QuranReciter reciter,
		QuranReciter textReciter)
	{
		this(new Dhikr(salat), reciter, textReciter, false);
	}

	public DhikrImam(QuranicVerse[] salat, QuranReciter reciter)
	{
		this(new Dhikr(salat), reciter, null);
	}

	public boolean isHeld()
	{
		return held;
	}

	public static DhikrImam getImamCandidate()
	{
		return imamCandidate;
	}

	public static void propose(DhikrImam imam)
	{
		DhikrImam.imamCandidate = imam;
	}

	public void setSkipProstration(boolean val)
	{
		this.skipProstration = val;
	}

	public Dhikr getDhikr()
	{
		return dhikr;
	}

	public void hold()
	{
		if (sigPause) return;
		held = true;
		sigTerm = true;
	}

	@Override
	public void revive()
	{
		if (held)
		{
			sigTerm = false;
			held = false;
		}
		sigPauseBeforeNext = false;
		super.revive();
	}

	private void preRecitation()
	{
		if (!sigTerm) reciteAudhubillah(reciter);
		if (!sigTerm && textReciter != null) reciteAudhubillah(textReciter);
		if (currentVerse.getVerseNumber() == 1
			&& currentVerse.getSectionNumber() != 9
			&& currentVerse.getSectionNumber() != 1)
		{
			if (!sigTerm) reciteBasmala(reciter);
			if (!sigTerm && textReciter != null) reciteBasmala(textReciter);
		}
		if (!sigTerm)
		{
			while (!readyForRecitation)
			{
				intelliSleep(10);
			}
		}
		if (held)
		{
			while (held)
			{
				SystemClock.sleep(10);
			}
			preRecitation();
		}
	}

	@Override
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
				boolean requireWifi = false;
				if (RecitationManager.getContext() != null
					&& SystemEssentialsAndroid.isPermissionGranted(
						RecitationManager.getContext(), permission.WAKE_LOCK))
				{
					QuranicVerse[] verses = dhikr.getAllVerses();
					for (int i = 0; i < verses.length; i++)
					{
						if (!reciter.getFile(verses[i]).exists())
						{
							requireWifi = true;
							break;
						}
						if (textReciter != null && !textReciter.getFile(verses[i]).exists())
						{
							requireWifi = true;
							break;
						}
					}
					verses = null;
				}
				if (requireWifi)
				{
					wifiLock = ((WifiManager) RecitationManager.getContext()
						.getSystemService(Context.WIFI_SERVICE)).createWifiLock(3,
						"thereisonlywe Salat API");
					wifiLock.acquire();
				}
				else forceUpdates = false;
				final int len = dhikr.getNumberOfVersesRemaining();
				if (downloadsFirst) // download all, return if not successful
				{
					if (!RecitationManager.updateAll(reciter, dhikr.getRemainingVerses(),
						forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
							: IOEssentials.FILE_UPDATE_POLICY_NEVER))
					{
						endSession();
						return;
					}
					else
					{
						readyForRecitation = true;
					}
				}
				else if (len > 1) // download first two verses
				{
					Thread prepareRecitationFiles = new Thread()
					{
						@Override
						public void run()
						{
							boolean success = false;
							for (int i = 0; i < len && i < 2; i++)
							{
								success = RecitationManager.updateAudioFile(reciter, dhikr
									.getNextNthVerse(i),
									forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
										: IOEssentials.FILE_UPDATE_POLICY_NEVER);
								if (success && textReciter != null) success = RecitationManager
									.updateAudioFile(textReciter, dhikr.getNextNthVerse(i),
										forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
											: IOEssentials.FILE_UPDATE_POLICY_NEVER);
								if (!success || sigTerm)
								{
									held = false;
									sigTerm = true;
									return;
								}
							}
							readyForRecitation = true;
						}
					};
					if (!sigTerm) new Thread(prepareRecitationFiles).start();
					else
					{
						endSession();
						return;
					}
				}
				else
				{
					readyForRecitation = true;
				}
				while (!readyForRecitation && !sigTerm)
					SystemClock.sleep(10);
				if (sigTerm)
				{
					endSession();
					return;
				}
				currentVerse = dhikr.getCurrentVerse();
				if (len > 1)
				{
					if (RecitationManager.getContext() != null
						&& SystemEssentialsAndroid.isPermissionGranted(
							RecitationManager.getContext(), permission.READ_PHONE_STATE))
					{
						tm = ((TelephonyManager) RecitationManager.getContext()
							.getSystemService(Context.TELEPHONY_SERVICE));
						tm.listen(psl, PhoneStateListener.LISTEN_CALL_STATE);
					}
					requestAudioFocus(afcl);
					if (sigTerm)
					{
						endSession();
						return;
					}
					preRecitation();
					status = STATUS_RECITING;
				}
				else
				{
					boolean b = true;
					if (currentVerse.getIndex() == 0 && !reciter.hasBasmala())
					{
						b = false;
					}
					else
					{
						b = RecitationManager.updateAudioFile(reciter, currentVerse,
							forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
								: IOEssentials.FILE_UPDATE_POLICY_NEVER);
						if (b && textReciter != null)
						{
							boolean c = RecitationManager.updateAudioFile(textReciter,
								currentVerse,
								forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
									: IOEssentials.FILE_UPDATE_POLICY_NEVER);
							if (requireTextRecitation) b = c;
						}
					}
					if (b)
					{
						requestAudioFocus(afcl);
						if (sigTerm)
						{
							held = false;
							sigTerm = true;
						}
						status = STATUS_RECITING;
					}
					else
					{
						held = false;
						sigTerm = true;
					}
				}
				while (held)
				{
					SystemClock.sleep(10);
				}
				intelliRecite();
			}
		}).start();
	}

	protected void downloadNextInQueue()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (ongoingDownload)
				{
					if (sigTerm) return;
					SystemClock.sleep(10);
				}
				if (sigTerm) return;
				final QuranicVerse v = (dhikr == null) ? null
					: dhikr.getNextNthVerse(2);
				if (sigTerm || v == null) { return; }
				ongoingDownload = true;
				RecitationManager.updateAudioFile(reciter, v,
					forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
						: IOEssentials.FILE_UPDATE_POLICY_NEVER);
				if (sigTerm || textReciter == null || !alive)
				{
					ongoingDownload = false;
					return;
				}
				RecitationManager.updateAudioFile(textReciter, v,
					forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
						: IOEssentials.FILE_UPDATE_POLICY_NEVER);
				ongoingDownload = false;
			}
		}).start();
	}

	private void intelliRecite()
	{
		while (!sigTerm && dhikr.getNumberOfVersesRemaining() > 0)
		{
			while (ongoingDownload)
				SystemClock.sleep(10);
			currentVerse = dhikr.getCurrentVerse();
			if (dhikr.getVerseCount() > 1 && !downloadsFirst) downloadNextInQueue();
			if (currentVerse.getIndex() == 0 && !reciter.hasBasmala())
			{
			}
			else reciteCurrentVerse();
			if (!sigTerm)
			{
				if (currentVerse.isAProstrationVerse()
					&& dhikr.getNumberOfVersesRemaining() > 1 && !skipProstration)
				{
					status = STATUS_WAITING_FOR_PROSTRATION;
					while (status == STATUS_WAITING_FOR_PROSTRATION && !skipProstration)
						intelliSleep(10);
				}
				if (dhikr.getNumberOfVersesRemaining() > 1 || loop)
				{
					while (sigPauseBeforeNext || sigPause)
					{
						SystemClock.sleep(10);
					}
				}
				if (!sigTerm) dhikr.next();
			}
			if (held)
			{
				while (held)
				{
					SystemClock.sleep(10);
				}
				preRecitation();
			}
		}
		if (loop && !sigTerm)
		{
			dhikr.rewind();
			intelliRecite();
		}
		else
		{
			endSession();
		}
	}

	@Override
	public boolean isPaused()
	{
		return sigPauseBeforeNext || sigPause;
	}

	public void pause()
	{
		sigPauseBeforeNext = true;
	}

	@Override
	public void terminate()
	{
		if (sigTerm) held = false;
		sigPauseBeforeNext = false;
		super.terminate();
	}

	@Override
	protected void endSession()
	{
		if (tm != null) tm.listen(psl, PhoneStateListener.LISTEN_NONE);
		tm = null;
		super.endSession();
		sigPauseBeforeNext = false;
		dhikr = null;
	}
}
