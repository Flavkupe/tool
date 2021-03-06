/**
 * This is the cpp end of the JNI connection between the tool and the cpp vision
 * algorithm. See the method processFrame() implemented below.
 *
 * @author Johannes Strom
 * @author Mark McGranagan
 *
 * @date November 2008
 */

#include <jni.h>

#include <time.h>
#include <sys/time.h>
#include <iostream>
#include <vector>
#include <string>

#include "edu_bowdoin_robocup_TOOL_Vision_TOOLVisionLink.h"
#include "Vision.h"
#include "NaoPose.h"
#include "Sensors.h"
using namespace std;


static long long
micro_time (void)
{
  // Needed for microseconds which we convert to milliseconds
  struct timeval tv;
  gettimeofday(&tv, NULL);

  return tv.tv_sec * 1000000 + tv.tv_usec;
}


/**
 *
 * This is the central cpp method called by the Java TOOL to run vision
 * results. It takes in the raw image data, as well as the joint angles
 * and single byte array representation of the color table.
 * (possibly later it will also need to take other sensor values.)
 *
 * Due to the difficulty of JNI, we currently also require that the thresholded
 * array which Java wants filled be passed in as well. This removes the need
 * for us to construct a java byte[][] from cpp.
 *
 * This method explicitly returns nothing. The results of vision computation
 * are sent back to Java via some setter methods. Right now we only send back
 * the thresholded array after vision is done with it. In the future,
 * we should make a method to translate a cpp field object into a
 * Data/FieldObject.java compatible field object.
 *
 * KNOWN INEFFICIENCIES:In the future, we'd like to be able
 * to store a static file-level pointer to the vision class. A method which
 * instantiates vision  be called in the TOOLVisionLink.java  contructor.
 * This instance of vision will also need to be destroyed somehow when
 * we are done with this VisionLink
 *
 */

JNIEXPORT void JNICALL Java_edu_bowdoin_robocup_TOOL_Vision_TOOLVisionLink_cppProcessImage
(JNIEnv * env, jobject jobj, jbyteArray jimg, jfloatArray jjoints,
 jbyteArray jtable, jobjectArray thresh_target){
    //Size checking -- we expect the sizes of the arrays to match
    //Base these on the size cpp expects for the image
    unsigned int tlenw =
        env->GetArrayLength((jbyteArray)
                            env->GetObjectArrayElement(thresh_target,0));
    //If one of the dimensions is wrong, we exit
    if(env->GetArrayLength(jimg) != IMAGE_BYTE_SIZE ||
       env->GetArrayLength(jjoints) != 22 ||
       env->GetArrayLength(jtable) != YMAX*UMAX*VMAX ||
       env->GetArrayLength(thresh_target) != IMAGE_HEIGHT ||
        tlenw != IMAGE_WIDTH){
        cout << "Error: One of the dimensions passed to " <<endl
             <<"   Java_TOOLVisionLink_processImage had incorrect dimensions"
             <<endl<<"   No Vision Processing Done" <<endl;
        return;//exit on error
    }

    //Instantiate the vision stuff
    Sensors sensors = Sensors();
    NaoPose pose = NaoPose(&sensors);
    Profiler profiler =  Profiler(&micro_time);
    Vision vision = Vision(&pose,&profiler);

    //load the table
    jbyte *buf_table = env->GetByteArrayElements( jtable, 0);
    byte * table = (byte *)buf_table; //convert it to a reg. byte array
    vision.thresh->initTableFromBuffer(table);
    env->ReleaseByteArrayElements( jtable, buf_table, 0);

    //Set the Senors data - Note: set visionBodyAngles not bodyAngles
    float * joints = env->GetFloatArrayElements(jjoints,0);
    vector<float> joints_vector = vector<float>(&joints[0],&joints[22]);
    env->ReleaseFloatArrayElements(jjoints,joints,0);
    sensors.setVisionBodyAngles(joints_vector);

    //get pointer access to the java image array
    jbyte *buf_img = env->GetByteArrayElements( jimg, 0);
    byte * img = (byte *)buf_img; //convert it to a reg. byte array
    //PROCESS VISION!!
    vision.notifyImage(img);
    vision.drawBoxes();
    env->ReleaseByteArrayElements( jimg, buf_img, 0);

    //Debug output:
    cout <<"Ball Width: "<<  vision.ball->getWidth() <<endl;
    cout<<"Pose Left Hor Y" << pose.getLeftHorizonY() <<endl;
    cout<<"Pose Right Hor Y" << pose.getRightHorizonY() <<endl;
    //copy results from vision thresholded to the array passed in from java
    //we access to each row in the java array, and copy in from cpp thresholded
    //we may in the future want to experiment with malloc, for increased speed
    for(int i = 0; i < IMAGE_HEIGHT; i++){
        jbyteArray row_target=
            (jbyteArray) env->GetObjectArrayElement(thresh_target,i);
        jbyte* row = env->GetByteArrayElements(row_target,0);

        for(int j = 0; j < IMAGE_WIDTH; j++){
            row[j]= vision.thresh->thresholded[i][j];
        }
        env->ReleaseByteArrayElements(row_target, row, 0);
    }


    return;

}
