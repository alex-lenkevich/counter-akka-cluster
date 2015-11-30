# counter-akka-cluster

### Create docker image
> sbt docker:publishLocal

### Run seed
> ./runSeed.sh [CONTAINER_NAME] [NODE_NUMBER] [NODE_COUNT] [SECRET]

### Run node(s)
> ./runNode.sh [SEED_ADDRESS] [NODE_NUMBER] [NODE_COUNT] [SECRET]

## Improvements:
- Nodes should check if message has been delivered
- Link docker containers together in order to not pass seed ip to nodes.
- Code clean-up
