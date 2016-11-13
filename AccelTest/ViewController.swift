//
//  ViewController.swift
//  AccelTest
//
//  Created by Tassilo Karge on 25.10.16.
//  Copyright © 2016 tassilokarge. All rights reserved.
//

import UIKit
import CoreMotion
import CorePlot
import AVFoundation

class ViewController: UIViewController, CPTPlotDataSource {

	let numRecords = 200

	let averageSize = 5

	let tapSize = 5
	let tapThreshold = 0.05

	var index = 0
	let torch = AVCaptureDevice.defaultDevice(withMediaType: AVMediaTypeVideo)!

	@IBOutlet weak var graphView1: CPTGraphHostingView!
	@IBOutlet weak var graphView2: CPTGraphHostingView!
	@IBOutlet weak var graphView3: CPTGraphHostingView!
	@IBOutlet weak var graphView4: CPTGraphHostingView!
	@IBOutlet weak var graphView5: CPTGraphHostingView!
	@IBOutlet weak var graphView6: CPTGraphHostingView!

	var graphViews : [CPTGraphHostingView] = []

	var movementValues : [CMDeviceMotion.MotionTuple] = []
	var averagedValues : [CMDeviceMotion.MotionTuple] = []

	override func viewDidLoad() {
		super.viewDidLoad()

		do { try torch.lockForConfiguration() } catch {}

		graphViews = [graphView1, graphView2, graphView3, graphView4, graphView5]
		let graphViewNames = ["x","y","z","α","β","γ"]

		for graphView in graphViews {
			graphView.hostedGraph = createGraph(for: graphView,
			                                    title: graphViewNames[graphViews.index(of: graphView)!])
		}

		AccellerometerController.sharedInstance.motionBlocks.append({ (motion) -> () in
			DispatchQueue.main.sync {

				//save value
				self.saveMovementValue(motion: motion)
				//detect taps
				self.detectTap(motion: motion)
				//additional processing
				//self.processNewRecord(motion: motion)
				//view updating
				self.updateGraphs()

				self.index = (self.index + 1) % self.numRecords

				if self.index == 0 {
					self.printExtremes()
				}
			}
		})
	}

	func detectTap(motion: CMDeviceMotion) {
		if index % tapSize == 0 {
			guard movementValues.count >= tapSize else { return }
			guard index > 0 || movementValues.count == numRecords else { return	}

			let slice : ArraySlice<CMDeviceMotion.MotionTuple>
			if index == 0 {
				slice = movementValues[(numRecords - tapSize - 1) ... (numRecords - 1)]
			} else {
				slice = movementValues[(index - tapSize) ... index]
			}

			let tapDetected = containsTap(slice)

			//output (ugly: color view)
			if tapDetected {
				self.view.backgroundColor = UIColor.green
				NSLog("tap")
				detectDoubleTap()
			} else {
				self.view.backgroundColor = UIColor.white
			}
		}
	}

	func detectDoubleTap() {

		var sliceEnds = [index == tapSize ? (numRecords - 1) : ((index + numRecords - tapSize) % numRecords)]
		for i in 1...5 {
			sliceEnds.append(sliceEnds[i-1] == tapSize
				? (numRecords - 1) : ((sliceEnds[i-1] + numRecords - tapSize) % numRecords))
		}

		//TODO: slices have overlap of 1 I think
		let slices = sliceEnds.map { (sliceEnd) -> ArraySlice<CMDeviceMotion.MotionTuple> in
			return movementValues[(sliceEnd - tapSize) ... sliceEnd]
		}
		let tapsDetected = slices.map(containsTap)

		var noTap : Bool = false
		for foundTap in tapsDetected {
			if noTap == false && foundTap == false {
				noTap = true
			} else if noTap == true && foundTap == true {
				//tap, then no tap then tap --> double tap
				NSLog("double tap")
				self.view.backgroundColor = UIColor.red
				if torch.torchMode == AVCaptureTorchMode.on {
					torch.torchMode = AVCaptureTorchMode.off
				} else {
					do { try torch.setTorchModeOnWithLevel(0.5) } catch {}
				}
				break;
			}
		}
	}

	func containsTap(_ slice : ArraySlice<CMDeviceMotion.MotionTuple>) -> Bool {
		let minimum : CMDeviceMotion.MotionTuple = slice.reduce((0,0,0,0,0,0)) {(res, new) -> CMDeviceMotion.MotionTuple in
			(min(res.0,new.0),min(res.1,new.1),min(res.2,new.2),min(res.3,new.3),min(res.4,new.4),min(res.5,new.5))
		}
		let maximum : CMDeviceMotion.MotionTuple = slice.reduce((0,0,0,0,0,0)) {(res, new) -> CMDeviceMotion.MotionTuple in
			(max(res.0,new.0),max(res.1,new.1),max(res.2,new.2),max(res.3,new.3),max(res.4,new.4),max(res.5,new.5))
		}

		//detection very simple: If value exceeds certain point, tap is detected
		if max(abs(minimum.0),abs(minimum.1),abs(minimum.2)) > tapThreshold {
			return true
		} else if max(maximum.0,maximum.1,maximum.2) > tapThreshold {
			return true
		} else {
			return false
		}
	}

	func processNewRecord(motion: CMDeviceMotion) {
		let averageRecords = numRecords / averageSize
		if index % averageSize == 0 {
			//calculate average over last numAverages of records
			guard movementValues.count >= averageSize else { return }
			guard index > 0 || movementValues.count == numRecords else { return	}

			let slice = index == 0
				? movementValues[(numRecords - averageSize - 1) ... (numRecords - 1)]
				: movementValues[(index - averageSize) ... index]
			let average = slice.reduce((0,0,0,0,0,0)) { (t1, t2) -> CMDeviceMotion.MotionTuple in
				(t1.0 + t2.0 / Double(averageSize), t1.1 + t2.1 / Double(averageSize), t1.2 + t2.2 / Double(averageSize),
				 t1.3 + t2.3 / Double(averageSize), t1.4 + t2.4 / Double(averageSize), t1.5 + t2.5 / Double(averageSize))
			}

			if averagedValues.count < averageRecords {
				averagedValues.append(average)
			} else {
				averagedValues[index/averageSize] = average
			}
		}
	}

	func saveMovementValue(motion : CMDeviceMotion) {
		let tuple = motion.tuple

		if movementValues.count < numRecords {
			movementValues.append(tuple)
		} else {
			movementValues[index] = tuple
		}
	}

	func updateGraphs() {
		for graphView in graphViews {
			let plot = graphView.hostedGraph!.plot(at: 0)!
			setNewPlotData(plot: plot)
			adjustPlotRange(plot: plot)
		}
	}

	func printExtremes() {
		let minimum : CMDeviceMotion.MotionTuple = movementValues.reduce((0,0,0,0,0,0)) {(res, new) -> CMDeviceMotion.MotionTuple in
				(min(res.0,new.0),min(res.1,new.1),min(res.2,new.2),min(res.3,new.3),min(res.4,new.4),min(res.5,new.5))
		}
		let maximum : CMDeviceMotion.MotionTuple = movementValues.reduce((0,0,0,0,0,0)) {(res, new) -> CMDeviceMotion.MotionTuple in
			(max(res.0,new.0),max(res.1,new.1),max(res.2,new.2),max(res.3,new.3),max(res.4,new.4),max(res.5,new.5))
		}

		NSLog("\nmin: \(String(format: "%.3f, %.3f, %.3f, %.3f, %.3f, %.3f, ", minimum.0, minimum.1, minimum.2, minimum.3, minimum.4, minimum.5))\nmax: \(String(format: "%.3f, %.3f, %.3f, %.3f, %.3f, %.3f, ", maximum.0, maximum.1, maximum.2, maximum.3, maximum.4, maximum.5))")
	}

	func createGraph(for view: UIView, title: String) -> CPTGraph {

		let graph = CPTXYGraph(frame: view.bounds)
		graph.title = title

		graph.apply(CPTTheme(named: kCPTPlainWhiteTheme))

		let frame = graph.plotAreaFrame!
		frame.paddingTop = 5
		frame.paddingLeft = 5
		frame.paddingRight = 5
		frame.paddingBottom = 5

		let axisSet = graph.axisSet as! CPTXYAxisSet
		let xAxis = axisSet.xAxis!
		xAxis.labelingPolicy = CPTAxisLabelingPolicy.none
		xAxis.orthogonalPosition = 0.0
		let yAxis = axisSet.xAxis!
		yAxis.labelingPolicy = CPTAxisLabelingPolicy.none
		yAxis.orthogonalPosition = 0.0

		let plotSpace = graph.defaultPlotSpace as! CPTXYPlotSpace

		let xRange = plotSpace.xRange.mutableCopy() as! CPTMutablePlotRange
		xRange.location = 0.0
		xRange.length = numRecords as NSNumber
		plotSpace.xRange = xRange
		let yRange = plotSpace.yRange.mutableCopy() as! CPTMutablePlotRange
		yRange.location = -0.5
		yRange.length = 1.0
		plotSpace.yRange = yRange

		let plot = CPTScatterPlot()
		plot.identifier = NSString(string: title)
		plot.dataSource = self
		plot.interpolation = CPTScatterPlotInterpolation.linear

		let style = plot.dataLineStyle!.mutableCopy() as! CPTMutableLineStyle
		style.lineWidth = 1.0
		style.lineColor = CPTColor(componentRed: 1, green: 0, blue: 0, alpha: 0.5)
		plot.dataLineStyle = style

		graph.add(plot)

		return graph
	}

	func setNewPlotData(plot : CPTPlot) {
		plot.deleteData(inIndexRange: NSRange(location: index, length: 1))
		plot.insertData(at: UInt(index), numberOfRecords: 1)
	}

	func adjustPlotRange(plot : CPTPlot) {
		plot.graph!.defaultPlotSpace?.scale(toFit: [plot])
	}

	//MARK plot data source

	func numberOfRecords(for: CPTPlot) -> UInt {
		return UInt(numRecords)
	}

	func number(for plot: CPTPlot, field: UInt, record rec: UInt) -> Any? {

		if Int(field) == CPTScatterPlotField.X.rawValue {

			return rec as NSNumber

		} else if (Int(rec) < movementValues.count) {

			switch plot {
			case graphView1.hostedGraph!.plot(at: 0)!:
				return movementValues[Int(rec)].0
			case graphView2.hostedGraph!.plot(at: 0)!:
				return movementValues[Int(rec)].1
			case graphView3.hostedGraph!.plot(at: 0)!:
				return movementValues[Int(rec)].2
			case graphView4.hostedGraph!.plot(at: 0)!:
				return movementValues[Int(rec)].3
			case graphView5.hostedGraph!.plot(at: 0)!:
				return movementValues[Int(rec)].4
			case graphView6.hostedGraph!.plot(at: 0)!:
				return movementValues[Int(rec)].5
			default:
				return 0
			}


			/*
			let avgPos = Int(rec)/averageSize
			switch plot {
			case graphView1.hostedGraph!.plot(at: 0)!:
				return movementValues[avgPos].0
			case graphView2.hostedGraph!.plot(at: 0)!:
				return movementValues[avgPos].1
			case graphView3.hostedGraph!.plot(at: 0)!:
				return movementValues[avgPos].2
			case graphView4.hostedGraph!.plot(at: 0)!:
				return movementValues[avgPos].3
			case graphView5.hostedGraph!.plot(at: 0)!:
				return movementValues[avgPos].4
			case graphView6.hostedGraph!.plot(at: 0)!:
				return movementValues[avgPos].5
			default:
				return 0
			}
			*/
		} else {

			return 0

		}
	}
}

