package Util;

public class Schedule<T extends Comparable<T>> {
	class Node {
		T element = null;
		Node next = null;
		public Node(T el) {
			element = el;
		}
	}
	Node first = null;
	int size = 0;
	T last_added = null;
	
	public T peek() { if( first == null) return null; return first.element; }
	public T poll() {
		if( first == null)
			return null;
		Node temp = first;
		first = first.next;
		size--;
		return temp.element;
	}
	public void remove(T to_remove) {
		if( first == null)
			return;
		Node prev = null;
		Node next = first;
		while( next != null) {
			if( next.element == to_remove)
				break;
			prev = next;
			next = next.next;
		}
		if( prev == null)
			first = next == null ? next : next.next;
		else
			prev.next = next == null ? next : next.next;
	}
	public void add(T to_add) {
		Node new_node = new Node(to_add);
		if( last_added == to_add) {
			System.out.println("scheduler: duplicate insertion detected! removing first!");
			remove( to_add);
		}
		last_added = to_add;

		Node prev = null;
		Node next = first;
		while( next != null && next.element.compareTo(to_add) < 0) {
			prev = next;
			next = next.next;
		}
		new_node.next = next;
		
		if( prev == null)
			first = new_node;
		else
			prev.next = new_node;
		size++;
	}
	public int size() { return size; }
}
