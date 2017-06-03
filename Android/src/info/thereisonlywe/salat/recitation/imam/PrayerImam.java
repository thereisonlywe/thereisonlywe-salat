package info.thereisonlywe.salat.recitation.imam;

import info.thereisonlywe.core.essentials.IOEssentials;
import info.thereisonlywe.quran.Quran;
import info.thereisonlywe.quran.QuranicVerse;
import info.thereisonlywe.salat.recitation.Prayer;
import info.thereisonlywe.salat.recitation.RecitationManager;

import java.util.ArrayList;

public class PrayerImam extends Imam {

	private final Prayer prayer;
	private boolean fileCheck = false;

	@SuppressWarnings("unused")
	private PrayerImam()
	{
		super();
		prayer = null;
	}

	public PrayerImam(Prayer prayer)
	{
		this.prayer = prayer;
		status = STATUS_FRESH;
		reciter = prayer.getReciter();
	}

	@Override
	public void begin()
	{
		alive = true;
		aliveImams.add(this);
		status = STATUS_INITIALIZING;
		Thread startPrayer = new Thread()
		{
			@Override
			public void run()
			{
				if (!fileCheck && !checkRequiredFiles()) return;
				status = STATUS_RECITING;
				setSalat(prayer);
				endSession();

			}
		};
		startPrayer.setPriority(Thread.MAX_PRIORITY);
		new Thread(startPrayer).start();
	}

	@Override
	public void terminate()
	{
		sigTerm = true;
		alive = false;
	}

	public boolean checkRequiredFiles()
	{
		if (!checkRequiredPrayerFiles()) return false;
		checkOptionalPrayerFiles();
		for (int j = 0; j < 7; j++)
		{
			if (sigTerm) return false;
			else
			{
				if (j == 0 && !reciter.hasBasmala()) continue;
				else if (!RecitationManager
						.updateAudioFile(
								reciter,
								Quran.getVerse(j),
								forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
										: IOEssentials.FILE_UPDATE_POLICY_NEVER))
					return false;
			}

		}
		ArrayList<QuranicVerse[]> verses = prayer.getAllVerses();
		for (int i = 0; i < verses.size(); i++)
		{
			for (int j = 0; j < verses.get(i).length; j++)
			{
				if (sigTerm
						|| !RecitationManager
								.updateAudioFile(
										reciter,
										verses.get(i)[j],
										forceUpdates ? IOEssentials.FILE_UPDATE_POLICY_IF_POSSIBLE
												: IOEssentials.FILE_UPDATE_POLICY_NEVER))
					return false;
			}
		}
		fileCheck = true;
		return true;
	}

	private void setSalat(Prayer prayer)
	{
		reciter = prayer.getReciter();
		intelliSleep(prayer.getPause(11));
		if (sigTerm) return;
		if (prayer.getIqamaRecitation() && IQAMA.exists()) recite(IQAMA);
		if (sigTerm) return;
		recite(TAKBEER);
		intelliSleep(prayer.getPause(0));
		if (sigTerm) return;

		for (int i = 0; i < prayer.getRakaatCount(); i++)
		{
			QuranicVerse[] verses = prayer.getVersesForRakat();
			// start with Fatiha
			if (reciter.hasBasmala())
			{
				recite(reciter.getFile(Quran.getVerse(0)));
				if (sigTerm) return;
			}
			recite(reciter.getFile(Quran.getVerse(1)));
			if (sigTerm) return;
			recite(reciter.getFile(Quran.getVerse(2)));
			if (sigTerm) return;
			recite(reciter.getFile(Quran.getVerse(3)));
			if (sigTerm) return;
			recite(reciter.getFile(Quran.getVerse(4)));
			if (sigTerm) return;
			recite(reciter.getFile(Quran.getVerse(5)));
			if (sigTerm) return;
			recite(reciter.getFile(Quran.getVerse(6)));
			intelliSleep(prayer.getPause(1)); // pause after Fatiha
			// recite verses of this rakaat
			for (int j = 0; j < verses.length; j++)
			{
				if (sigTerm) return;
				recite(reciter.getFile(verses[j]));
			}
			intelliSleep(prayer.getPause(2));
			if (sigTerm) return;
			recite(TAKBEER);
			intelliSleep(prayer.getPause(3)); // wait at rkh
			recite(SAMIALLAH);
			intelliSleep(prayer.getPause(4)); // wait at second kym
			if (sigTerm) return;
			recite(TAKBEER);
			intelliSleep(prayer.getPause(5));// wait at first sjd
			if (sigTerm) return;
			recite(TAKBEER);
			intelliSleep(prayer.getPause(6)); // sit
			if (sigTerm) return;
			recite(TAKBEER);
			intelliSleep(prayer.getPause(7)); // wait at second sjd
			if (sigTerm) return;
			recite(TAKBEER);
			if (i + 1 == prayer.getRakaatCount())
			{ // end salat
				intelliSleep(prayer.getPause(10)); // sit at the end
				if (sigTerm) return;
				recite(SALAM);
				intelliSleep(2000);
				return;
			}

			else if (i % 2 == 0)
			{
				intelliSleep(prayer.getPause(8)); // after sjd
			}

			else
			// if i % 2 == 1
			{
				intelliSleep(prayer.getPause(9)); // middle sit
				if (sigTerm) return;
				recite(TAKBEER);
				intelliSleep(prayer.getPause(8));
			}

			prayer.next();
		}

		if (sigTerm) return;
		if (prayer.getDhikrRecitation() && DHIKR.exists()) recite(DHIKR);
	}

}
