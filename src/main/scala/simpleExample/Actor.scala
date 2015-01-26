package simpleExample

import akka.actor._
import akka._

class CommandBusActor(transactor:ActorRef, accactor:ActorRef) extends Actor with CommandBus{
	def receive = {	
	  case command:TransferCommitCommand => accactor ! command; transactor ! command		  							    
	  case command:WithdrawCommitCommand => accactor ! command; transactor ! command
	  case command:DepositCommitCommand => accactor ! command; transactor ! command
	  case command:RegisterAccountCommand => accactor ! command; transactor ! command
	  case command:DeleteAccountCommand => accactor ! command; transactor ! command
	  case command:Command => transactor ! command
	  case _ => logger.warn("Wrong Type of Command")
	}
}

class TransActor(eventbusactor:ActorRef, handler:TransCommandHandler) extends Actor{
	def receive = {
	  case command:Command =>	handler.receive(command)
			  					val events = handler.getEvents
			  					if (events != Nil)
			  						eventbusactor ! handler.getEvents
			  					handler.markEventsCommit
	  case _ => logger.warn("Transaction Actor get wrong list of events")
	}
	
}

class AccActor(eventbusactor:ActorRef, handler:AccCommandHandler) extends Actor{
	def receive = {
	  case command:Command => 	handler.receive(command)
			  					val events = handler.getEvents
			  					if (events != Nil)
			  						eventbusactor ! handler.getEvents
			  					handler.markEventsCommit
	  case _ => logger.warn("Account Actor receive an error message.")
	}
}

class EventBusActor() extends Actor{
	def receive = {
	  case events:List[Event] => logger.info("EventBus receives a event stream")
			  						events.foreach(ev=>logger.info(ev.toString))
	  case _ => logger.warn("Error messages are sent to the Mongo-Actor")
	}
}