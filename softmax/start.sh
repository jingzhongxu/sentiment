#!/bin/bash

time java -cp ./bin:./lib/mongo.jar SoftmaxRegressionWithMongo > train.result
echo "---------------------------" >> train.result
time java -cp bin:./lib/mongo.jar SoftmaxRegressionBaseline >> train.result