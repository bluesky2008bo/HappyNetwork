/*
 * Copyright 2013 Peng fei Pan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.xiaopan.easynetwork.android.image;

/**
 * 这是一个圆圈
 */
public class Circle<T> {
	/**
	 * 最大容量
	 */
	private int maxSize;
	/**
	 * 头节点
	 */
	private Node headerNode;
	/**
	 * 尾节点
	 */
	private Node footerNode;
	/**
	 * 容量
	 */
	private int size;
	
	/**
	 * 创建一个圆圈，同时你必须指定它的最大容量
	 * @param maxSize 最大容量
	 */
	public Circle(int maxSize){
		setMaxSize(maxSize);
	}
	
	/**
	 * 放入一个对象
	 * @param object
	 */
	public void put(T object){
		//如果还没有满
		if(size < maxSize){
			//如果头节点还是空的，说明当前是空的
			if(headerNode == null){
				headerNode = new Node(object);
				footerNode = headerNode;
			}else{
				Node endNode = new Node(object);
				footerNode.setNext(endNode);
				footerNode = endNode;
			}
			size++;
		}else{
			Node endNode = new Node(object);
			footerNode.setNext(endNode);
			footerNode = endNode;
			headerNode = headerNode.getNext();
		}
	}
	
	/**
	 * 删除一个对象
	 * @return 被删除的对象
	 */
	@SuppressWarnings("unchecked")
	public T remove(){
		T object = null;
		if(headerNode != null){
			object = (T) headerNode.getObject();
			headerNode = headerNode.getNext();
			size--;
		}
		return object;
	}
	
	/**
	 * 清空
	 */
	public void clear(){
		headerNode = null;
		footerNode = null;
		size = 0;
	}
	
	/**
	 * 当前大小
	 * @return 当前大小
	 */
	public int size(){
		return size;
	}

	/**
	 * 获取最大容量
	 * @return 最大容量
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * 设置最大容量
	 * @param maxSize 最大容量
	 */
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	
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
}
