package cz.vutbr.fit.layout.bcs.impl;

import java.util.Date;

public class StopWatch
{
	private boolean running;
	private long start;
	private long total;
	private final boolean nano;

	public StopWatch()
	{
		this.running = false;
		this.start = 0;
		this.total = 0;
		this.nano = false;
	}

	public StopWatch(boolean nano)
	{
	    this.running = false;
        this.start = 0;
        this.total = 0;
	    this.nano = nano;
	}

	public void toggle()
	{
		long now;

		if (this.nano)
		{
		    now = System.nanoTime();
		}
		else
		{
		    now = (new Date()).getTime();
		}

		if (this.running)
		{
			if (start != 0) this.total += now-start;
			this.start = 0;
		}
		else if (!this.running)
		{
			this.start = now;
		}

		this.running = !this.running;
	}

	public void toggleAndDrop()
	{
	    this.start = 0;
	    this.running = false;
	}

	public void clear()
	{
	    this.running = false;
	    this.start = 0;
	    this.total = 0;
	}

	public void addTime(long t)
	{
	    if (this.running) return;

	    this.total += t;
	}

	public long getTotal()
	{
		return this.total;
	}
}
