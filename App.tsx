import React, {useRef, useState, useEffect, useMemo} from 'react';
import {StyleSheet, Text, View, TouchableOpacity, Alert} from 'react-native';
import {VisionCameraProxy} from 'react-native-vision-camera';
import {
  Camera,
  runAtTargetFps,
  useCameraDevice,
  useFrameProcessor,
} from 'react-native-vision-camera';
import {useCameraPermission} from 'react-native-vision-camera';
import {useImageLabeler} from 'react-native-vision-camera-v3-image-labeling';

function App() {
  const camera = useRef<Camera>(null);
  const [hasPermission, setHasPermission] = useState(false);
  const device = useCameraDevice('back', {
    physicalDevices: [
      'ultra-wide-angle-camera',
      'wide-angle-camera',
      'telephoto-camera',
    ],
  });
  const {hasPermission: cameraPermission, requestPermission} =
    useCameraPermission();

  const devices = Camera.getAvailableCameraDevices();
  console.log('🚀 ~ App ~ devices:', devices);

  useEffect(() => {
    (async () => {
      const status = await requestPermission();
      setHasPermission(status);
    })();
  }, [requestPermission]);

  // -------------------------------------------- FRAME PROCESSO

  const frameProcessor = useFrameProcessor(frame => {
    'worklet';
    console.log(`Frame: ${frame.width}x${frame.height} (${frame.pixelFormat})`);
  }, []);

  const {scanImage} = useImageLabeler();
  const frameProcessorImageLabel = useFrameProcessor(frame => {
    'worklet';
    runAtTargetFps(2, () => {
      const data = scanImage(frame);
      console.log(data, 'data');
    });
  }, []);

  const plugin = useMemo(
    () => VisionCameraProxy.initFrameProcessorPlugin('processImage', {}),
    [],
  );

  const processImage = useFrameProcessor(
    frame => {
      'worklet';
      runAtTargetFps(2, () => {
        if (plugin == null) {
          throw new Error('Failed to load Frame Processor Plugin!');
        }
        const processed = plugin.call(frame, {test: 'test  '});
        console.log('Processed frame:', processed);
        return processed;
      });
    },
    [plugin],
  );

  // -------------------------------------

  const takePhoto = async () => {
    if (camera.current) {
      try {
        const photo = await camera.current.takePhoto({
          flash: 'off',
          enableShutterSound: true,
        });
        Alert.alert('Photo saved to', photo.path);
      } catch (e) {
        Alert.alert(
          'Error taking photo',
          e instanceof Error ? e.message : 'Unknown error',
        );
      }
    }
  };

  if (!hasPermission) {
    return (
      <View style={styles.container}>
        <Text>Camera permission not granted</Text>
      </View>
    );
  }

  if (!device) {
    return (
      <View style={styles.container}>
        <Text>Camera device not available</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Camera
        ref={camera}
        style={StyleSheet.absoluteFill}
        device={device}
        isActive={true}
        photo={true}
        frameProcessor={processImage}
        pixelFormat="yuv"
        enableFpsGraph={true}
      />
      <TouchableOpacity style={styles.captureButton} onPress={takePhoto} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'red',
  },
  captureButton: {
    position: 'absolute',
    bottom: 40,
    alignSelf: 'center',
    width: 70,
    height: 70,
    borderRadius: 35,
    backgroundColor: 'white',
    borderWidth: 5,
    borderColor: '#f0f0f0',
  },
});

export default App;
