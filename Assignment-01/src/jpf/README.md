# JPF verification for Assignment-01

This folder contains model-checking drivers for the concurrent monitor used by Assignment-01.

## What is verified

- `TestBoundedBufferSafety`: Model-checks the custom `BoundedBuffer` implementation for Producer-Consumer safety, ensuring no elements are lost or duplicated, and validates the absence of deadlocks in a finite execution constraint.
- `TestBoundedBufferPollNonBlocking`: Verifies the specific non-blocking semantics of `poll(0)`, ensuring it returns immediately without blocking the thread even when the buffer is full.
- `TestCollisionDeadlock`: Model-checks the concurrent collision resolution logic, specifically verifying the anti-deadlock lock ordering mechanism (based on `System.identityHashCode()`) between interacting balls.
- `TestCyclicBarrier`: Validates the correct synchronization behavior of the `CyclicBarrier` used in the collision resolution, ensuring that no thread proceeds until all have reached the barrier.

## Docker setup and test execution
### Build image and start container + mount

```bash
cd ~/LocalProjects/PCD/jpf-core
docker compose build
docker compose run --rm -it -v $HOME/LocalProjects/PCD/pcd-assignments/Assignment-01:/ass -v $HOME/LocalProjects/PCD/pcd-assignments/Assignment-01/src/jpf:/pcd-jpf jpf-dev
```

### Compile
```bash
rm -rf /pcd-jpf/target/jpf-classes
mkdir -p /pcd-jpf/target/jpf-classes

javac --release 11 -d /pcd-jpf/target/jpf-classes -classpath /pcd-jpf/target/jpf-classes:/home/jpf-core/build/jpf.jar /ass/src/main/java/pcd/poool/common/util/*.java /ass/src/main/java/pcd/poool/common/model/*.java /pcd-jpf/java/pcd/poool/jpf/*.java
```

### Tests
```bash
cd /pcd-jpf

java -jar /home/jpf-core/build/RunJPF.jar java/pcd/poool/jpf/TestBoundedBufferSafety.jpf
java -jar /home/jpf-core/build/RunJPF.jar java/pcd/poool/jpf/TestBoundedBufferPollNonBlocking.jpf
java -jar /home/jpf-core/build/RunJPF.jar java/pcd/poool/jpf/TestCollisionDeadlock.jpf
java -jar /home/jpf-core/build/RunJPF.jar java/pcd/poool/jpf/TestCyclicBarrier.jpf
```