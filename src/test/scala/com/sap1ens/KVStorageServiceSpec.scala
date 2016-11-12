package com.sap1ens

class KVStorageServiceSpec extends BaseActorSpec {

  import com.sap1ens.KVStorageService._

  behavior of "KV Storage Service"

  it should "get, set and delete elements" in {
    val exampleService = system.actorOf(KVStorageService.props(), "KVStorageService")

    exampleService ! Get("test")

    expectMsg(Result(None))

    exampleService ! Set("test", "12345")

    expectMsg(Updated("test"))

    exampleService ! Get("test")

    expectMsg(Result(Some("12345")))

    exampleService ! Delete("test")

    expectMsg(Deleted("test"))

    exampleService ! Get("test")

    expectMsg(Result(None))
  }

}
