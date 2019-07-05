#!/usr/bin/env bash

~/Work/tools/spark-2.4.3-bin-hadoop2.7/bin/spark-submit \
    --master k8s://https://192.168.0.151:8443/k8s/clusters/c-slscp  \
    --deploy-mode cluster \
    --name demo-spark \
    --class org.grozeille.App \
    --conf spark.kubernetes.namespace=spark \
    --conf spark.kubernetes.node.selector.spark-role=executor \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
    --conf spark.executor.instances=5 \
    --conf spark.eventLog.enabled=true \
    --conf spark.eventLog.dir=hdfs://lenovo01/tmp/spark-logs \
    --conf spark.kubernetes.container.image=grozeille/spark:latest \
    hdfs://lenovo01/user/root/spark-demo-1.0-SNAPSHOT.jar


~/Work/tools/spark-2.4.3-bin-hadoop2.7/bin/spark-submit \
    --master local[3]  \
    --name demo-spark \
    --class org.grozeille.App \
    target/spark-demo-1.0-SNAPSHOT.jar

