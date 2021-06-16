package cn.lenovo.cwnisface.face.realtime;

/**
 * Created by baohm1 on 2018/3/5.
 */

public class FrameManager {
    public static final int FRAME_BUFFER_NUM = 3;	//不能小于3
    private static final int FRAME_INDEX_NULL = -1;

    private long mBufferCounter = 0;
    private long mLastCounter = 0;
    private int lastDeueueBuffer = -1;

    public FrameBuffer[] frameBuffers = new FrameBuffer[FRAME_BUFFER_NUM];

    public class FrameBuffer {
        public byte[] dataVis;
        public byte[] dataNis;
        public int lengthVis;
        public int lengthNis;
//        public int w;
//        public int h;
//        public int format;
        public volatile boolean isVaild = false;
        public volatile boolean isBusy = false;
        public long counter = 0;    //计数器，会一直累加
        public int index = 0;
    }

    public FrameManager() {
        for(int i=0; i<FRAME_BUFFER_NUM; i++) {
            frameBuffers[i] = new FrameBuffer();
            frameBuffers[i].index = i;
        }
    }

    /**
     * 获取用于写的buffer
     * @return
     */
    public synchronized FrameBuffer queueBuffer() {
        long bufCounter = Long.MAX_VALUE;
        int index = FRAME_INDEX_NULL;
        //如果有空闲且没有数据的buffer，
        for(int i=0; i<FRAME_BUFFER_NUM; i++) {
            if (frameBuffers[i].isBusy == false && frameBuffers[i].isVaild == false) {
                return markFrameBuffer(i);
            }
        }

        //如果有空闲的buffer，则记录，选出counter最小的buffer（最旧的数据）。。
        for(int i=0; i<FRAME_BUFFER_NUM; i++) {
            if (frameBuffers[i].isBusy == false) {
                if (bufCounter > frameBuffers[i].counter) {
                    index = i;
                    bufCounter = frameBuffers[i].counter;
                }
            }
        }
        if (bufCounter != 0) {
            return markFrameBuffer(index);
        }

        //超过两个buffer出现busy,说明有线程没有正常释放buffer，则需要手动释放
        for(int i=0; i<FRAME_BUFFER_NUM; i++) {
            if (i != lastDeueueBuffer) {
                frameBuffers[i].isBusy = false;
                frameBuffers[i].isVaild = false;
            }
        }
        System.out.println("====!!!!!!!!====Handle clear flag============>");

        return markFrameBuffer((lastDeueueBuffer+1)%FRAME_BUFFER_NUM);
    }

    /**
     * 获取用于读的buffer
     * @return
     */
    public synchronized FrameBuffer dequeueBuffer() {
        long bufCounter = 0;
        int lastIndex = FRAME_INDEX_NULL;
        //如果有空闲且有数据的buffer，则记录，选出counter最大的buffer（最新）。
        for(int i=0; i<FRAME_BUFFER_NUM; i++) {
            if (frameBuffers[i].isBusy == false && frameBuffers[i].isVaild == true) {
                if (bufCounter < frameBuffers[i].counter) {
                    if (lastIndex != FRAME_INDEX_NULL) {
                        //index帧不是最新帧，丢弃此帧数据
                        frameBuffers[lastIndex].isVaild = false;
                    }
                    lastIndex = i;
                    bufCounter = frameBuffers[i].counter;
                }
            }
        }
        if (bufCounter != 0 && mLastCounter < bufCounter) {
            lastDeueueBuffer = lastIndex;
            mLastCounter = bufCounter;
            frameBuffers[lastIndex].isBusy = true;
            return frameBuffers[lastIndex];
        }
        return null;
    }

    public void resetFrameBuffer() {
        for(int i=0; i<FRAME_BUFFER_NUM; i++) {
            frameBuffers[i].isBusy = false;
            frameBuffers[i].isVaild = false;
            frameBuffers[i].counter = 0;
        }
        mBufferCounter = 0;
        mLastCounter = 0;
        lastDeueueBuffer = 0;
    }

    private FrameBuffer markFrameBuffer(int index) {
        mBufferCounter++;
        frameBuffers[index].counter = mBufferCounter;
        frameBuffers[index].isBusy = true;
        return frameBuffers[index];
    }
}

