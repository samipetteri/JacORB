package org.jacorb.test.notification;

import org.omg.CORBA.IntHolder;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventChannelAdmin.TypeError;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.ProxyType;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushSupplier;
import org.omg.CosNotifyChannelAdmin.SequenceProxyPushSupplierHelper;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.SequencePushConsumerHelper;
import org.omg.CosNotifyComm.SequencePushConsumerOperations;
import org.omg.CosNotifyComm.SequencePushConsumerPOATie;

class SequencePushReceiver extends Thread implements SequencePushConsumerOperations, TestClientOperations
{
    SequenceProxyPushSupplier pushSupplier_;
    boolean received_ = false;
    boolean connected_ = false;
    long timeout_ = 2000;

    NotificationTestCase testCase_;

    public SequencePushReceiver(NotificationTestCase testCase) {
        testCase_ = testCase;
    }

    public void run()
    {
        synchronized (this)
        {
            if (!received_)
            {
                try
                {
                    wait(timeout_);
                }
                catch (InterruptedException e)
                {}
            }
        }
    }

    public void push_structured_events(StructuredEvent[] event) throws Disconnected
    {
        synchronized (this)
        {
            received_ = true;
            notifyAll();
        }
    }

    public void disconnect_sequence_push_consumer()
    {
        connected_ = false;
    }

    public void offer_change(EventType[] type1, EventType[] type2) throws InvalidEventType
        {}

    public void connect(EventChannel channel,
                        boolean useOrSemantic)
        throws AdminLimitExceeded,
               AlreadyConnected,
               TypeError
    {
        SequencePushConsumerPOATie receiverTie = new SequencePushConsumerPOATie(this);
        ConsumerAdmin _consumerAdmin = channel.default_consumer_admin();
        IntHolder _proxyIdHolder = new IntHolder();

        pushSupplier_ = SequenceProxyPushSupplierHelper.narrow(_consumerAdmin.obtain_notification_push_supplier(ClientType.SEQUENCE_EVENT, _proxyIdHolder));

        testCase_.assertEquals(ProxyType._PUSH_SEQUENCE,
                           pushSupplier_.MyType().value());


        pushSupplier_.connect_sequence_push_consumer(SequencePushConsumerHelper.narrow(receiverTie._this(testCase_.getORB())));

        connected_ = true;
    }

    public boolean isEventHandled()
    {
        return received_;
    }

    public boolean isConnected()
    {
        return connected_;
    }

    public boolean isError()
    {
        return false;
    }

    public void shutdown()
    {
        pushSupplier_.disconnect_sequence_push_supplier();
    }
}
