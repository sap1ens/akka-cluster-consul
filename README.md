# Bootstrapping Akka Cluster With Consul

If you want to start using Akka Cluster you should solve cluster bootstrapping first. Almost every tutorial on the internet (including the [official one](http://doc.akka.io/docs/akka/2.4/scala/cluster-usage.html#Joining_to_Seed_Nodes)) tells you to use seed nodes. It looks something like this:

```
akka.cluster.seed-nodes = [
  "akka.tcp://ClusterSystem@host1:2552",
  "akka.tcp://ClusterSystem@host2:2552"
]
```

but wait… Hardcoding nodes manually? Now when we have Continuous Delivery, Immutable Infrastructure, tools like CloudFormation and Terraform, and of course Containers?!

Well, Akka Cluster also provides programmatic API for bootstrapping:

```scala
def joinSeedNodes(seedNodes: Seq[Address]): Unit
```

So, instead of defining seed nodes manually we’re going to use service discovery with Consul to register all nodes after startup and use provided API to create a cluster programmatically. Let’s do it!

## TL;DR

It’s very easy to try (with Docker):

```
$ git clone https://github.com/sap1ens/akka-cluster-consul.git
$ docker-compose up
```

Docker Compose will start 6 containers:

- 3 for Consul Cluster
- 3 for Akka Cluster

Everything should just work and in about 15 seconds after startup you should see a few `Cluster is ready!` messages in logs - it worked!

More details with explanations can be found here: [http://sap1ens.com/blog/2016/11/12/bootstrapping-akka-cluster-with-consul/](http://sap1ens.com/blog/2016/11/12/bootstrapping-akka-cluster-with-consul/). 
