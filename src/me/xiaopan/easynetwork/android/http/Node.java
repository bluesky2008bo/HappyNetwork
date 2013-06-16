package me.xiaopan.easynetwork.android.http;

/**
 * 节点
 */
public class Node {
	/**
	 * 当前节点存储的对象
	 */
	private Object object;
	/**
	 * 下一个节点
	 */
	private Node next;
	/**
	 * 上一个节点
	 */
	private Node last;
	
	/**
	 * 创建一个节点
	 * @param object 当前节点存储的对象
	 */
	public Node(Object object){
		setObject(object);
	}
	
	/**
	 * 创建一个节点
	 * @param object 当前节点存储的对象
	 * @param next 下一个节点
	 */
	public Node(Object object, Node next){
		setObject(object);
		setNext(next);
	}
	
	/**
	 * 创建一个节点
	 * @param object 当前节点存储的对象
	 * @param next 下一个节点
	 * @param last 上一个节点
	 */
	public Node(Object object, Node next, Node last){
		setObject(object);
		setNext(next);
		setLast(last);
	}
	
	/**
	 * 获取当前节点存储的对象
	 * @return 当前节点存储的对象
	 */
	public Object getObject() {
		return object;
	}
	
	/**
	 * 设置当前节点存储的对象
	 * @param object 当前节点存储的对象
	 */
	public void setObject(Object object) {
		this.object = object;
	}
	
	/**
	 * 获取下一个节点
	 * @return 下一个节点
	 */
	public Node getNext() {
		return next;
	}
	
	/**
	 * 设置下一个节点
	 * @param next 下一个节点
	 */
	public void setNext(Node next) {
		this.next = next;
	}
	
	/**
	 * 获取上一个节点
	 * @return 上一个节点
	 */
	public Node getLast() {
		return last;
	}
	
	/**
	 * 设置上一个节点
	 * @param last 上一个节点
	 */
	public void setLast(Node last) {
		this.last = last;
	}
}