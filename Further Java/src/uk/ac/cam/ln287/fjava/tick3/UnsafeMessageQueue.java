package uk.ac.cam.ln287.fjava.tick3;

public class UnsafeMessageQueue<T> implements MessageQueue<T> {

	private static class Link<L> {
		L val;
		Link<L> next;
		Link(L val) { this.val = val; this.next = null; }
	}
	private Link<T> first = null;
	private Link<T> last = null;

	public void put(T val) {
		Link<T> link = new Link<T>(val);
		if (first == null) {
			first = link;
			last = first;
		}else {
			last.next = link;
			last = last.next;
		}
	}

	public T take() {
		while(first == null) //use a loop to block thread until data is available
			try {
				Thread.sleep(100);
			} catch(InterruptedException ie) {
			}
		T result = first.val;
		first = first.next;
		if(first == null) last = null;
		return result;
	}
}