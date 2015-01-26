package simpleExample

import scala.collection.mutable.HashMap
import CQRS.AggregateRoot

class InMemoryPersistency[T] {
	var hashmap = HashMap[String, T]()
	
	def save(id:GUID, obj:T):Unit={
		hashmap.put(id.toString,obj)
	}
	
	def get(id:GUID):Option[T]={
		hashmap.get(id.toString)
	}
	
	def contain(id:GUID):Boolean={
		hashmap.contains(id.toString)
	}

	def remove(id:GUID):Unit={
		hashmap.remove(id.toString)
	}
}
