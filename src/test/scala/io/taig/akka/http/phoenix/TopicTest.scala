package io.taig.akka.http.phoenix

class TopicTest extends Suite {
    it should "parse regular topics" in {
        Topic.parse( "foo:bar" ) shouldBe Some( Topic( "foo", "bar" ) )
    }

    it should "parse broadcasts" in {
        Topic.parse( "foo" ) shouldBe Some( Topic( "foo" ) )
    }

    it should "fail to parse invalid topics" in {
        Topic.parse( "foo:bar:baz" ) shouldBe None
        Topic.parse( "foo:" ) shouldBe None
        Topic.parse( "" ) shouldBe None
        Topic.parse( " " ) shouldBe None
    }
}