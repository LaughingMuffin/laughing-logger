package org.laughing.logger.reader;


public abstract class AbsLogcatReader implements LogcatReader {

    protected boolean recordingMode;

    public AbsLogcatReader(boolean recordingMode) {
        this.recordingMode = recordingMode;
    }

    public boolean isRecordingMode() {
        return recordingMode;
    }
}
