package simjava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;

import simjava.AST.Dec;

public class SymbolTable {

	int current_scope, next_scope;
	Hashtable<String, HashMap<Integer, Dec>> hashtable = new Hashtable<>();
	Stack<Integer> scope_stack = new Stack<>();

	/**
	 * to be called when block entered
	 */
	public void enterScope() {
		current_scope = ++next_scope;
		scope_stack.push(current_scope);
	}

	/**
	 * leaves scope
	 */
	public void leaveScope() {
		scope_stack.pop();
		if (scope_stack.size() != 0) {
			current_scope = scope_stack.peek();
		}
		// else throw new StackOverflowError();
	}

	public boolean insert(String ident, Dec dec) {
		HashMap<Integer, Dec> hashmap = new HashMap<Integer, Dec>();
		if (hashtable.get(ident) != null) {
			hashmap = hashtable.get(ident);
			if (hashmap.get(current_scope) == null) {
				hashmap.put(current_scope, dec);
				hashtable.put(ident, hashmap);
			} else {
				return false;

			}

		} else {
			hashmap.put(current_scope, dec);
			hashtable.put(ident, hashmap);
		}

		return true;
	}

	public Dec lookup(String ident) {
		HashMap<Integer, Dec> hashmap = new HashMap<Integer, Dec>();
		ArrayList<Integer> arr = new ArrayList<>();
		Dec dec = null;
		if (hashtable.containsKey(ident)) {
			hashmap = hashtable.get(ident);
			for (Integer i : scope_stack) {
				arr.add(i);
			}
			for (int i = arr.size() - 1; i >= 0; i--) {
				if (hashmap.get(arr.get(i)) != null) {
					dec = hashmap.get(arr.get(i));
					break;
				}
			}

		}
		return dec;
	}

	public SymbolTable() {
		current_scope = 0;
		next_scope = 0;
		scope_stack.push(next_scope);
	}

	@Override
	public String toString() {
		return "";
	}

}
