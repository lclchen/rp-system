package simpleExample

import akka.actor._
import akka._
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.redis._

object RunExample {
	def main(args: Array[String]): Unit = {
		val system = ActorSystem("actorsystem")
		
		val mongoClient1 = MongoClient()
		val db1 = mongoClient1("RSTransation")
		val mongoClient2 = MongoClient()
		val db2 = mongoClient1("RSAccount")
		
		val mongoTrans:MongoPersistence = new MongoPersistence(db1)
		mongoTrans.ensureindex()
		val evstorage:EventStore = new EventStore(mongoTrans)
		val transrepository:TransRepository = new TransRepository(evstorage:EventStore, new InMemoryPersistency[TransactionAgg]())
		val transhandler:TransCommandHandler = new TransCommandHandler(transrepository)
		
		val mongoAcc:MongoPersistence = new MongoPersistence(db2)
		mongoAcc.ensureindex()
		val accstorage:AccStore = new AccStore(mongoAcc)
		val accevstorage:EventStore =new EventStore(mongoAcc)
		val accrepository:AccRepository = new AccRepository(accstorage:AccStore, accevstorage:EventStore, new InMemoryPersistency[AccountAgg]())
		val acchandler:AccCommandHandler = new AccCommandHandler(accrepository)
  	
		val eventbusActor = system.actorOf(Props(new EventBusActor()), name ="EventBusActor")
		val accActor = system.actorOf(Props(new AccActor(eventbusActor,acchandler)), name ="AccountActor")
		val transActor = system.actorOf(Props(new TransActor(eventbusActor, transhandler)), name ="TransactionActor")
		val commandbusActor = system.actorOf(Props(new CommandBusActor(transActor, accActor)), name ="CommandBusActor")
		
		//***************** Test ***************
		val uuid_tom:GUID = java.util.UUID.randomUUID();
		val uuid_peter:GUID = java.util.UUID.randomUUID();
		val uuid_james:GUID = java.util.UUID.randomUUID();
		def newGuid = java.util.UUID.randomUUID();
		
		val mongotest = new MongoPersistence(db2)
		val account1 = new AccountAgg(uuid_tom, "Tom", 1000) // Tom's balance = 1000
		val account2 = new AccountAgg(uuid_peter, "Peter", 2000)// Peter's blance = 2000		
		mongotest.addSnapShot(account1)
		mongotest.addSnapShot(account2)
		
		
		//
		logger.info("\n\n=======start=======\n")
		
		//Test withdraw
		val trans1 = newGuid
		commandbusActor ! new WithdrawStartCommand(trans1, new Date(), uuid_tom, 1)//withdraw 1 RMB in Tom's account
		commandbusActor ! new WithdrawCommitCommand(trans1, new Date(), uuid_tom, 1)//commit
		
		val trans2 = newGuid
		commandbusActor ! new WithdrawStartCommand(trans2, new Date(), uuid_tom, 1)
		commandbusActor ! new WithdrawCancelCommand(trans2, new Date(), uuid_tom, 1)//cancel
		
		val trans3 = newGuid
		commandbusActor ! new WithdrawStartCommand(trans3, new Date(), uuid_tom, 1)
		commandbusActor ! new WithdrawFailCommand(trans3, new Date(), uuid_tom, 1)//fail
		
		//deposit
		val trans4 = newGuid
		commandbusActor ! new DepositStartCommand(trans4, new Date(), uuid_tom, 2)//deposit 2 RMB
		commandbusActor ! new DepositCommitCommand(trans4, new Date(), uuid_tom, 2)//finish
		
		//transfer
		val trans5 = newGuid
		commandbusActor ! new TransferStartCommand(trans5, new Date(), uuid_tom, uuid_peter, 10)//transfer 10 RMB from Tom to Peter
		commandbusActor ! new TransferCommitCommand(trans5, new Date(), uuid_tom, uuid_peter, 10)//finish
		
		//create and delete account
		val trans6 = newGuid
		val trans7 = newGuid
		commandbusActor ! new RegisterAccountCommand(trans6, new Date(), uuid_james, "James")
		commandbusActor ! new DeleteAccountCommand(trans7, new Date(), uuid_tom)
		
		Thread.sleep(2000)
		system.shutdown()
	}
}