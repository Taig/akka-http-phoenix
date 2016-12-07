package io.taig.akka.http.phoenix

class TopicTest extends Suite {
    "parse" should "handle regular topics" in {
        Topic.parse( "foo:bar" ) shouldBe Some( Topic( "foo", "bar" ) )
    }

    it should "handle broadcasts" in {
        Topic.parse( "foo" ) shouldBe Some( Topic( "foo" ) )
    }

    it should "fail to handle invalid topics" in {
        Topic.parse( "foo:bar:baz" ) shouldBe None
        Topic.parse( "foo:" ) shouldBe None
        Topic.parse( "" ) shouldBe None
        Topic.parse( " " ) shouldBe None
    }

    "isSubscribedTo" should "accept equal topics" in {
        val topic = Topic( "foo", "bar" )
        topic isSubscribedTo topic shouldBe true
    }

    it should "accept specific topics with broadcasts" in {
        Topic( "foo", "bar" ) isSubscribedTo Topic( "foo" ) shouldBe true
    }

    it should "deny invalid topics" in {
        Topic( "foo", "bar" ) isSubscribedTo Topic( "bar", "baz" ) shouldBe false
        Topic( "foo", "bar" ) isSubscribedTo Topic( "bar" ) shouldBe false
    }
}