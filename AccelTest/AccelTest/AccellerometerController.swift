//
//  CoreMotionController.swift
//  AccelTest
//
//  Created by Tassilo Karge on 25.10.16.
//  Copyright © 2016 tassilokarge. All rights reserved.
//

import Foundation
import CoreMotion

class AccellerometerController {

	static let sharedInstance = AccellerometerController()

	public typealias MotionBlock = (CMDeviceMotion)->()

	let refreshFrequency = 0.01
	let motionManager = CMMotionManager()
	let motionQueue = OperationQueue()
	public var motionBlocks : [MotionBlock] = []

	init() {
		motionQueue.qualityOfService = QualityOfService.userInteractive
		motionManager.accelerometerUpdateInterval = refreshFrequency
		motionManager.startDeviceMotionUpdates(to: motionQueue, withHandler: updateMotion)
	}

	func updateMotion(d:CMDeviceMotion?, e:Error?) -> () {
		guard  let d = d else {NSLog("error: \(e?.localizedDescription)"); return}
		for closure in motionBlocks {
			closure(d)
		}
	}
}

extension CMDeviceMotion {
	public typealias MotionTuple = (Double, Double, Double, Double, Double, Double)
	var tuple : MotionTuple {
		return (self.userAcceleration.x,
		 self.userAcceleration.y,
		 self.userAcceleration.z,
		 self.attitude.roll,
		 self.attitude.pitch,
		 self.attitude.yaw)
	}
}
