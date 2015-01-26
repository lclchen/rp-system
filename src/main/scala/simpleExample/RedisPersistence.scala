package simpleExample

//import redis.clients.util._
//import redis.clients.jedis._
import com.redis._
import java.lang.String

class RedisPersistence(redis:RedisClient) extends InMemoryPersistency[TransactionAgg]{
	override def save(transid:GUID, obj:TransactionAgg):Unit={
		var map = Map("id"-> obj.id,
					"transactiontype"->obj.transactiontype,
					"account"->obj.account,
					"otheraccount"->obj.otheraccount,
					"state"->obj.state,
					"username"->obj.username,
					"amount"->obj.amount,
					"activated"->obj.activated,
					"revision"->obj.getRevision)
		redis.hmset(transid.toString, map)
	}
	
	override def get(transid:GUID):Option[TransactionAgg]={
		if(!contain(transid))
			return None
		val result = redis.hmget(transid.toString,"transactiontype","account","otheraccount","state",
								"username","amount","activated","revision").get 
		val trans:TransactionAgg = new TransactionAgg(java.util.UUID.fromString(result.get("id").get), result.get("transactiontype").get,java.util.UUID.fromString((result.get("account").get)))
		trans.otheraccount = java.util.UUID.fromString((result.get("otheraccount").get))
		trans.state = result.get("state").get
		trans.username = result.get("username").get
		trans.amount = java.lang.Integer.parseInt(result.get("amount").get)
		trans.activated = java.lang.Boolean.parseBoolean(result.get("activated").get)
		trans.setRevision(java.lang.Integer.parseInt(result.get("revision").get))
		Some(trans)
	}
	
	override def contain(transid:GUID):Boolean={
		redis.exists(transid.toString)
	}

	override def remove(transid:GUID):Unit={
		redis.del(transid.toString)
	}
	
}