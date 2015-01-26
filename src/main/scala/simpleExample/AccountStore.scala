package simpleExample

import com.mongodb._

class AccStore(mongo:MongoPersistence) {
	def saveAccountAgg(account:AccountAgg):Unit={
	    mongo.saveSnapShot(account)
	}
	
	def getAccountAgg(accountid:GUID):Option[AccountAgg]={
	    mongo.getSnapshot(accountid)
	}
	
	def addAccountAgg(account:AccountAgg):Unit={
		mongo.addSnapShot(account)
	}
}