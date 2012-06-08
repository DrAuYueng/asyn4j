package com.googlecode.asyn4j.disruptor;

import com.googlecode.asyn4j.core.work.AsynWork;
import com.googlecode.asyn4j.disruptor.test.DeliveryReport;
import com.googlecode.asyn4j.disruptor.test.DeliveryReportEvent;
import com.googlecode.asyn4j.disruptor.test.DisruptorHelper;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.SingleThreadedClaimStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;

public class Helper {
	/**
	 * ringbuffer�����������2��N�η�
	 */
	private static final int BUFFER_SIZE = 1024 * 8;
	private RingBuffer<AsynEvent> ringBuffer;
	private SequenceBarrier sequenceBarrier;
	private AsynEventHandler handler;
	private BatchEventProcessor<AsynEvent> batchEventProcessor;
	private static Helper instance;
	private static boolean inited = false;

	private Helper() {
		ringBuffer = new RingBuffer<AsynEvent>(
				AsynEvent.EVENT_FACTORY,
				new SingleThreadedClaimStrategy(BUFFER_SIZE),
				new YieldingWaitStrategy());
		sequenceBarrier = ringBuffer.newBarrier();
		handler = new AsynEventHandler();
		batchEventProcessor = new BatchEventProcessor<AsynEvent>(
				ringBuffer, sequenceBarrier, handler);
		ringBuffer.setGatingSequences(batchEventProcessor.getSequence());
	}

	public static void initAndStart() {
		instance = new Helper();
		new Thread(instance.batchEventProcessor).start();
		inited = true;
	}

	public static void shutdown() {
		if (!inited) {
			throw new RuntimeException("Disruptor��û�г�ʼ����");
		}
		instance.shutdown0();
	}

	private void shutdown0() {
		batchEventProcessor.halt();
	}

	private void produce0(AsynWork work) {
		// ��ȡ��һ�����к�
		long sequence = ringBuffer.next();
		// ��״̬�������ringBuffer�ĸ����к���
		ringBuffer.get(sequence).setWork(work);
		// ֪ͨ�����߸���Դ��������
		ringBuffer.publish(sequence);
	}

	/**
	 * ��״̬���������Դ���У��ȴ�����
	 * 
	 * @param deliveryReport
	 */
	public static void produce(AsynWork work) {
		if (!inited) {
			throw new RuntimeException("Disruptor��û�г�ʼ����");
		}
		instance.produce0(work);
	}
}