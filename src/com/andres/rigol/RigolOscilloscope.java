package com.andres.rigol;

import jvisa.JVisa;
import jvisa.JVisaReturnBytes;
import jvisa.JVisaReturnString;

public class RigolOscilloscope {

    private final JVisa jVisa;

    public enum PointMode {
        RAW, NORMAL, MAX
    }
    public enum MemoryDepth {
        LONG, NORMAL
    }

    public RigolOscilloscope(JVisa jVisa){
        this.jVisa = jVisa;
    }

    public void stopSampling(){
        jVisa.write(":STOP");
        try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    public void runSampling(){
        jVisa.write(":RUN");
    }

    /**
     * This command sets the mode of waveform points. This defines the number of points
     *  we get when querying for data.
     * - Normal mode:
     *      Math: 600 points
     *      FFT: 512 points
     *      Channels: 600 points
     *      Half-Channel: 600 points
     *      Digital: 600 points
     * - RAW mode (L is when we use long memory, N is when using normal memory):
     *      Math: 600 points L and N
     *      FFT: 512 points in L and N
     *      Channels: 8192 (8K) points in N and 524288 (512K) in L
     *      Half-Channel: 16384 (16K) points in N and 1048576 (1M) in L
     *      Digital: 16384 (16K) points in N and 1048576 (1M) in L
     * - MAX mode:
     *      In RUN state, MAX is the same as NORMAL. In STOP state, MAX is the same as RAW
     */
    public void setPointMode(PointMode pointMode){
        switch (pointMode){
            case RAW:
                jVisa.write(":WAV:POIN:MODE RAW");
                break;
            case NORMAL:
                jVisa.write(":WAV:POIN:MODE NORMAL");
                break;
            case MAX:
                jVisa.write(":WAV:POIN:MODE MAX");
                break;
        }
    }

    public PointMode getPointMode(){
        jVisa.write(":WAV:POIN:MODE?");
        JVisaReturnString r = new JVisaReturnString();
        jVisa.read(r);

        if(r.returnString.equalsIgnoreCase("RAW")) return PointMode.RAW;
        else if(r.returnString.equalsIgnoreCase("NORMAL")) return PointMode.NORMAL;
        else if(r.returnString.equalsIgnoreCase("MAXIMUM")) return PointMode.MAX;
        else return null;
    }

    public void forceTrigger(){
        jVisa.write(":KEY:FORCE");
    }

    /**
     * Release the scope so "Rmt" is not showed in the screen anymore and we can
     *  use it normally, otherwise all keys are locked until we press the force
     *  trigger button
     */
    public void releaseScope(){
        forceTrigger();
    }

    public byte[] getChannelData(int channelNumber){
        if(channelNumber < 0 || channelNumber > 2) channelNumber = 1;
        jVisa.write(":WAV:DATA? CHAN" + channelNumber);

        JVisaReturnBytes response = new JVisaReturnBytes();
        // Read a very large number of bytes to ensure we get everything from the scope.
        // The max number of bytes we receive should be 1048586 bytes.
        jVisa.read(response, 2000000);

        return response.returnBytes;
    }

    /**
     * Set the scope time scale. Valid Strings are for example:
     *  50ns, 2s, 100us, 500us, etc
     * @param timeScale
     */
    public void setTimeScale(String timeScale){
        jVisa.write(":TIM:SCAL " + timeScale);
    }

    public double getTimeScale(){
        jVisa.write(":TIM:SCAL?");
        JVisaReturnString returnString = new JVisaReturnString();
        jVisa.read(returnString);

        return Double.parseDouble(returnString.returnString);
    }

    /**
     * Set the scope time scale offset. Valid Strings are for example:
     *  50ns, 2s, 100us, 500us, etc
     * @param offset
     */
    public void setTimescaleOffset(String offset){
        jVisa.write(":TIM:OFFS " + offset);
    }

    public double getTimescaleOffset(){
        jVisa.write(":TIM:OFFS?");
        JVisaReturnString returnString = new JVisaReturnString();
        jVisa.read(returnString);

        return Double.parseDouble(returnString.returnString);
    }

    /**
     * Set the channel voltage offset. Valid Strings are for example:
     *  50mV, 1V, 500mV, 50V, etc
     * @param channelNumber
     * @param offset
     */
    public void setVoltageOffset(int channelNumber, String offset){
        jVisa.write(":CHAN" + channelNumber + ":OFFS " + offset);
    }

    public double getVoltageOffset(int channelNumber){
        jVisa.write(":CHAN" + channelNumber + ":OFFS?");
        JVisaReturnString returnString = new JVisaReturnString();
        jVisa.read(returnString);

        return Double.parseDouble(returnString.returnString);
    }

    /**
     * Set the voltage scale for the given channel
     * When the Probe is set to 1X, the range of <range> is 2mV ~ 10V;
     * When the Probe is set to 5X, the range of <range> is 10mV ~50V;
     * When the Probe is set to 10X, the range of <range> is 20mV ~ 100V;
     * When the Probe is set to 50X, the range of <range> is 100mV ~ 500V;
     * When the Probe is set to 100X, the range of <range> is 200mV ~ 1000V;
     * When the Probe is set to 500X, the range of <range> is 1V ~5000V;
     * When the Probe is set to 1000X, the range of <range> is 2V~ 10000V
     * @param channelNumber channel to set
     * @param scale
     */
    public void setVoltageScale(int channelNumber, String scale){
        jVisa.write(":CHAN" + channelNumber + ":SCAL " + scale);
    }

    public double getVoltageScale(int channelNumber){
        jVisa.write(":CHAN" + channelNumber + ":SCAL?");
        JVisaReturnString returnString = new JVisaReturnString();
        jVisa.read(returnString);

        return Double.parseDouble(returnString.returnString);
    }

    /**
     * Set the probe attenuation for the given channel. Valid values are
     *  1, 5, 10, 50, 100, 500 and 1000.
     * @param channelNumber
     * @param attenuation
     */
    public void setProbeAttenuation(int channelNumber, int attenuation){
        jVisa.write(":CHAN" + channelNumber + ":PROB " + attenuation);
    }

    public int getProbeAttenuation(int channelNumber){
        jVisa.write(":CHAN" + channelNumber + ":PROB?");
        JVisaReturnString returnString = new JVisaReturnString();
        jVisa.read(returnString);

        return (int)Double.parseDouble(returnString.returnString);
    }

    public void setMemoryDepth(MemoryDepth memoryDepth){
        jVisa.write(":ACQ:MEMD " + memoryDepth);
    }

    public MemoryDepth getMemoryDepth(){
        jVisa.write(":ACQ:MEMD?");
        JVisaReturnString returnString = new JVisaReturnString();
        jVisa.read(returnString);

        return MemoryDepth.valueOf(returnString.returnString);
    }

    /**
     * Sets the trigger level. Valid Strings are for example:
     *  50mV, 1V, 500mV, 50V, etc. The range of the trigger level
     *  goes from -6*(Voltage scale) to 6*(Voltage scale).
     * @param level
     */
    public void setTriggerLevel(String level){
        jVisa.write(":TRIG:EDGE:LEV " + level);
    }

    public double getTriggerLevel(){
        jVisa.write(":TRIG:EDGE:LEV?");
        JVisaReturnString returnString = new JVisaReturnString();
        jVisa.read(returnString);

        return Double.parseDouble(returnString.returnString);
    }
}
