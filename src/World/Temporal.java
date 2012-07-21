package World;

public abstract class Temporal implements iTemporal, Comparable<Temporal> {
	
	long last_execution_time = 0;
	long next_execution_time = 0;
	boolean is_scheduled = false;
	public int compareTo(Temporal a) {
		//int order = 1;
		return next_execution_time>a.next_execution_time ? 1 : next_execution_time<a.next_execution_time ? -1 : 0;
	}
	public void schedule(long t) {
		if( is_scheduled)
			Time.eventQueue.remove(this);
		next_execution_time = t;
		is_scheduled = true;
		Time.eventQueue.add(this);
	}
	public void unschedule() {
		if( is_scheduled)
			Time.eventQueue.remove(this);
		is_scheduled = false;
	}
	
	public long getNextExecutionTime() { return next_execution_time; }
}
