package com.haoyou.spring.cloud.alibaba;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import com.haoyou.spring.cloud.alibaba.commons.domain.RedisKey;
import com.haoyou.spring.cloud.alibaba.commons.entity.Protocol;
import com.haoyou.spring.cloud.alibaba.commons.mapper.ProtocolMapper;
import com.haoyou.spring.cloud.alibaba.commons.util.RedisKeyUtil;
import com.haoyou.spring.cloud.alibaba.controller.BeaconController;
import com.haoyou.spring.cloud.alibaba.redis.service.ScoreRankService;

import com.haoyou.spring.cloud.alibaba.util.RedisObjectUtil;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.BaseImageRecordReader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.datavec.image.transform.*;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import sun.misc.BASE64Decoder;
import sun.security.provider.X509Factory;


import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ManagerTest {
    private final static Logger logger = LoggerFactory.getLogger(ManagerTest.class);
    @Autowired
    ScoreRankService scoreRankService;
    @Autowired
    private RedisObjectUtil redisObjectUtil;
    @Autowired
    private ProtocolMapper protocolMapper;



    @Test
    public void contextLoads() throws Exception {

    }


    public static void main( String[] args ) throws IOException {
        int rngSeed = 123; // random number seed for reproducibility
        int batchSize = 128; // batch size for each epoch
        int numEpochs = 15; // number of epochs to perform

//        MultiLayerNetwork model = creatModel(rngSeed);
        MultiLayerNetwork model = MultiLayerNetwork.load(FileUtil.file("D:/MNIST.model"), true);

//        ImageTransform transform = new MultiImageTransform(new ShowImageTransform("After transform"));
//
//        NativeImageLoader loader = new NativeImageLoader(28, 28, 1,transform);
//
//        INDArray image = loader.asMatrix(FileUtil.file("D:/1567677611.jpg"));
//
//        Console.log(image);
//
//        INDArray output = model.output(image);
//
//        Console.log(output);
//        image.close();
//        output.close();
//        train(model,batchSize,rngSeed,numEpochs);
        evaluate(model,batchSize,rngSeed);
//        model.save(FileUtil.file("D:/MNIST.model"));


        Console.log("****************Example finished********************");
    }
    private static MultiLayerNetwork getModel() throws IOException {
        return MultiLayerNetwork.load(FileUtil.file("D:/MNIST.model"),true);
    }


    private static void train(MultiLayerNetwork model,int batchSize,int rngSeed,int numEpochs) throws IOException {
        //Get the DataSetIterators:
        DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, rngSeed);
        Console.log("Train model....");
        model.fit(mnistTrain, numEpochs);
    }

    private static void evaluate(MultiLayerNetwork model,int batchSize,int rngSeed) throws IOException {


        DataSetIterator mnistTest = new MnistDataSetIterator(batchSize, false, rngSeed);
        mnistTest.next();
        DataSet next = mnistTest.next();
        INDArray features = next.getFeatures();
        Console.log(features.shapeInfoToString());

        ImageTransform transform = new MultiImageTransform(new ShowImageTransform("After transform"));
        NativeImageLoader loader = new NativeImageLoader(28, 28, 1,transform);
        features = loader.asMatrix(FileUtil.file("D:/1567677611.jpg"));
        float[][][][] featureData = new float[1][1][28][28];
        int j = -1;
        for(int i = 0;i<28*28;i++){
            float aFloat = features.getFloat(i);
//            aFloat = ((int) aFloat) & 0xFF;
            if (aFloat > 30.0f)
                aFloat = 0.0f;
            else
                aFloat = 1.0f;

            if(i%28 == 0){
                j++;
            }
            featureData[0][0][j][i%28] = aFloat;
        }
        features = Nd4j.create(featureData);
        Console.log(features);
        INDArray output = model.output(features);

        double m = 0D;
        int r = -1;
        for(int i = 0;i<10;i++){
            double aDouble = output.getDouble(i);
            if(aDouble>m){
                m=aDouble;
                r = i;
            }
        }

        Console.log(r);
        features.close();
        output.close();
//        Console.log("Evaluate model....");
//        Evaluation eval = model.evaluate(mnistTest);
//        Console.log(eval.stats());
    }

    private static MultiLayerNetwork creatModel(int rngSeed) throws IOException {
        //number of rows and columns in the input pictures
        final int numRows = 28;
        final int numColumns = 28;
        int outputNum = 10; // number of output classes


        Console.log("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(rngSeed) //include a random seed for reproducibility
                // use stochastic gradient descent as an optimization algorithm
                .updater(new Nesterovs(0.006, 0.9))
                .l2(1e-4)
                .list()
                .setInputType(InputType.convolutional(numRows,numColumns,1))
                .layer(new DenseLayer.Builder() //create the first, input layer with xavier initialization
                        .nIn(numRows * numColumns)
                        .nOut(1000)
                        .activation(Activation.RELU)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD) //create hidden layer
                        .nIn(1000)
                        .nOut(outputNum)
                        .activation(Activation.SOFTMAX)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        //print the score with every 1 iteration
//        model.setListeners(new ScoreIterationListener(1));


        return model;


    }


}
