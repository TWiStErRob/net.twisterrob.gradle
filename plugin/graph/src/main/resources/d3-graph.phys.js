var phys = function() {
	return {
		lineX: lineX,
		lineY: lineY,
		collide: collide,
		angle: angle
	};
	function overlap (a, b) {
		var topLeft = a.x < b.x && b.x < a.x2() && a.y < b.y && b.y < a.y2();
		var bottomRight = a.x < b.x2() && b.x2() < a.x2() && a.y < b.y2() && b.y2() < a.y2();
		var topRight = a.x < b.x2() && b.x2() < a.x2() && a.y < b.y && b.y < a.y2();
		var bottomLeft = a.x < b.x && b.x < a.x2() && a.y < b.y2() && b.y2() < a.y2();
		return topLeft || bottomRight || topRight || bottomLeft;
	};
	function collide (n) {
		var padding = 10,
			nx1 = n.x - padding,
			nx2 = n.x2() + padding,
			ny1 = n.y - padding,
			ny2 = n.y2() + padding;
		return function(quad, x1, y1, x2, y2) {
			var res = (nx2 < x1 || x2 < nx1) || (ny2 < y1 || y2 < ny1);
			/*console.log("collide " + quad.point + " " + Math.round(x1) + "," + Math.round(y1) + " - " + Math.round(x2) + "," + Math.round(y2)
				+ " to " + n.id + " " + Math.round(nx1) + "," + Math.round(ny1) + " - " + Math.round(nx2) + "," + Math.round(ny2)
				+ ": " + res);*/
			var dx, dy;
			var q = quad.point;
		    if (q && (q !== n)) {
				if (overlap(n, q)) {
					correct(n, q);
				}
		    }
		    return res;
		};
	};
	function correct (n, q) {
		// q < n (q's right overlaps n's left, move q left, -)
		var diffLeft = q.x2() - n.x; // n.x is bigger
		// n < q (q's left overlaps n's right, move q right, +)
		var diffRight = n.x2() - q.x;
		var dx;
		if (Math.abs(diffLeft) < Math.abs(diffRight)) {
			dx = diffLeft;
		} else {
			dx = diffRight;
		}
		dx = dx / 2;
		n.x -= dx;
		q.x += dx;
		dy = Math.min(n.y2() - q.y, q.y2() - n.y) / 8;
		n.y -= dy;
		q.y += dy;
	};
	function angle (start, end) {
		var dy = end.y - start.y;
		var dx = end.x - start.x;
		var theta = Math.atan2(dy, dx); // range (-PI, PI]
		theta *= 180 / Math.PI; // rads to degs, range (-180, 180]
		//if (theta < 0) theta = 360 + theta; // range [0, 360)
		return theta;
	};
	// TODO make it finer by 22.5 degree units
	function lineX (start, end, flip) {
		var x, theta = angle(start, end);
		if(flip) {
			theta = -theta;
			var temp = start;
			start = end;
			end = temp;
		}
		if (-45 <= theta && theta <= -0 || 0 <= theta && theta <= 45) {
			x = flip? start.x : start.x2(); // right
		} else if (45 <= theta && theta <= 135) {
			x = (start.x + start.x2()) / 2; // top middle
		} else if (135 <= theta && theta <= 180 || -180 <= theta && theta <= -135) {
			x = flip? start.x2() : start.x; // left
		} else if (-135 <= theta && theta <= -45) {
			x = (start.x + start.x2()) / 2; // bottom middle
		} else {
			throw theta + " is out of range (0..360)";
		}
		return x - start.width / 2;
	};
	function lineY (start, end, flip) {
		var y, theta = angle(start, end);
		if(flip) {
			theta = -theta;
			var temp = start;
			start = end;
			end = temp;
		}
		if (-45 <= theta && theta <= -0 || 0 <= theta && theta <= 45) {
			y = (start.y + start.y2()) / 2; // right middle
		} else if (45 <= theta && theta <= 135) {
			y = start.y2(); // top
		} else if (135 <= theta && theta <= 180 || -180 <= theta && theta <= -135) {
			y = (start.y + start.y2()) / 2; // left middle
		} else if (-135 <= theta && theta <= -45) {
			y = start.y; // bottom
		} else {
			throw theta + " is out of range (0..360)";
		}
		return y - start.height / 2;
	};
}();
