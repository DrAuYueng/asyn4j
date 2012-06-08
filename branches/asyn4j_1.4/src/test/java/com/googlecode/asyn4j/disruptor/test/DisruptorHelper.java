package com.googlecode.asyn4j.disruptor.test;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.SingleThreadedClaimStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;

public class DisruptorHelper {
    /**
     * ringbuffer�����������2��N�η�
     */
    private static final int BUFFER_SIZE = 1024 * 8;
    private RingBuffer<DeliveryReportEvent> ringBuffer;
    private SequenceBarrier sequenceBarrier;
    private DeliveryReportEventHandler handler;
    private BatchEventProcessor<DeliveryReportEvent> batchEventProcessor;
    private  static DisruptorHelper instance;
    private static boolean inited = false;
    private DisruptorHelper(){
        ringBuffer = new RingBuffer<DeliveryReportEvent>(
                DeliveryReportEvent.EVENT_FACTORY, new SingleThreadedClaimStrategy(
                        BUFFER_SIZE), new YieldingWaitStrategy());
        sequenceBarrier = ringBuffer.newBarrier();
        handler = new DeliveryReportEventHandler();
        batchEventProcessor = new BatchEventProcessor<DeliveryReportEvent>(ringBuffer, sequenceBarrier, handler);
        ringBuffer.setGatingSequences(batchEventProcessor.getSequence());
    }

    public static void initAndStart(){
        instance = new DisruptorHelper();
        new Thread(instance.batchEventProcessor).start();
        inited = true;
    }

    public static void shutdown(){
        if(!inited){
            throw new RuntimeException("Disruptor��û�г�ʼ����");
        }
        instance.shutdown0();
    }

    private void shutdown0(){
        batchEventProcessor.halt();
    }
    private void produce0(DeliveryReport deliveryReport) {
        //��ȡ��һ�����к�
        long sequence = ringBuffer.next();
        //��״̬�������ringBuffer�ĸ����к���
        ringBuffer.get(sequence).setDeliveryReport(deliveryReport);
        //֪ͨ�����߸���Դ��������
        ringBuffer.publish(sequence);
    }

    /**
     * ��״̬���������Դ���У��ȴ�����
     * @param deliveryReport
     */
    public static void produce(DeliveryReport deliveryReport) {
        if(!inited){
            throw new RuntimeException("Disruptor��û�г�ʼ����");
        }
        instance.produce0(deliveryReport);
    }
}