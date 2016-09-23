package com.cennavi.impor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class RealtimeTraf2Redis extends Thread {

	private Properties prop = new Properties();

	private String topic;
	
	private Jedis jedis;
	
	private Pipeline pipeline;
	
	private int num = 0;

	public RealtimeTraf2Redis(String dir) throws FileNotFoundException,
			IOException {
		prop.load(new FileInputStream(dir));

		topic = prop.getProperty("topic");
		
		jedis = new Jedis(prop.getProperty("redis_ip"),Integer.parseInt(prop.getProperty("redis_port")),Integer.parseInt(prop.getProperty("redis_timeout")));
		
		jedis.auth(prop.getProperty("redis_author"));
		
		jedis.select(Integer.parseInt(prop.getProperty("redis_db")));
		
		pipeline = jedis.pipelined();
	}
	
	

	@Override
	public void run() {

		ConsumerConnector consumer = createConsumer();
		
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(topic, 1); // 一次从主题中获取一个数据
		Map<String, List<KafkaStream<byte[], byte[]>>> messageStreams = consumer
				.createMessageStreams(topicCountMap);
		KafkaStream<byte[], byte[]> stream = messageStreams.get(topic).get(0);// 获取每次接收到的这个数据
		ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
		
		
		
		while (iterator.hasNext()) {
			
			byte[] bytes = iterator.next().message();
			
			String[] splits = new String(bytes).split(",");
			
			pipeline.set(splits[0], splits[1]);
			
			num++;
			
			if (num % 100 == 0){
				pipeline.sync();
			}
		}
	}
	
	

	private ConsumerConnector createConsumer() {
		Properties properties = new Properties();
		properties.put("zookeeper.connect", prop.getProperty("zoo_ip") + ":"
				+ prop.getProperty("zoo_port"));// 声明zk
		properties.put("group.id", "group1");// 必须要使用别的组名称，
												// 如果生产者和消费者都在同一组，则不能访问同一组内的topic数据
		return Consumer.createJavaConsumerConnector(new ConsumerConfig(
				properties));
	}

	public static void main(String[] args) throws Exception {

		if (args != null && args.length > 0) {

			new GdbKafka2Redis(args[0]).start();

		} else {
			System.out.println("Warn:input your config file!!!");
		}

	}


}
