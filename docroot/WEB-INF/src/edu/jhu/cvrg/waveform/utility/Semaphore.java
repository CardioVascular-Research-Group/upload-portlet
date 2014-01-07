package edu.jhu.cvrg.waveform.utility;

public class Semaphore {
	  private int signals = 0;
	  private int bound   = 0;

	  private static Semaphore createFolderSemaphore = null;
	  
	  public static Semaphore getCreateFolderSemaphore(){
		  if(createFolderSemaphore == null){
			  createFolderSemaphore = new Semaphore(1);
		  }
		  return createFolderSemaphore;
	  }	  
	  
	  private Semaphore(int upperBound){
	    this.bound = upperBound;
	  }

	  public synchronized void take() throws InterruptedException{
	    while(this.signals == bound) wait();
	    this.signals++;
	    this.notify();
	  }

	  public synchronized void release() throws InterruptedException{
	    while(this.signals == 0) wait();
	    this.signals--;
	    this.notify();
	  }
	}
