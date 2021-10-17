var phys = function() {
	return {
		pointOnRect: function (rect, point) {
			var w = rect.width / 2;
			var h = rect.height / 2;
			return pointOnRect(point.x, point.y, rect.x - w, rect.y - h, rect.x + w, rect.y + h);
		},
		collide: collide
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
	/**
	 * Finds the intersection point between
	 *     * the rectangle
	 *       with parallel sides to the x and y axes
	 *     * the half-line pointing towards (x,y)
	 *       originating from the middle of the rectangle
	 *
	 * Note: the function works given min[XY] <= max[XY],
	 *       even though minY may not be the "top" of the rectangle
	 *       because the coordinate system is flipped.
	 *
	 * @param (x,y) point to build the line segment from
	 * @param minX the "left" side of the rectangle
	 * @param minY the "top" side of the rectangle
	 * @param maxX the "right" side of the rectangle
	 * @param maxY the "bottom" side of the rectangle
	 * @param check whether to treat point inside the rect as error
	 * @return an object with x and y members for the intersection
	 * @throws if check == true and (x,y) is inside the rectangle
	 * @author TWiStErRob
	 * @see <a href="http://stackoverflow.com/a/18292964/253468">based on</a>
	 */
	function pointOnRect (x, y, minX, minY, maxX, maxY, check) {
		//assert minX <= maxX;
		//assert minY <= maxY;
		if (check && (minX <= x && x <= maxX) && (minY <= y && y <= maxY))
			throw "Point " + [x,y] + "cannot be inside "
			    + "the rectangle: " + [minY, minY] + " - " + [maxX, maxY] + ".";
		var midX = (minX + maxX) / 2;
		var midY = (minY + maxY) / 2;
		// if (midX - x == 0) -> m == ±Inf -> minYx/maxYx == x (because value / ±Inf = ±0)
		var m = (midY - y) / (midX - x);

		if (x <= midX) { // check "left" side
			var minXy = m * (minX - x) + y;
			if (minY < minXy && minXy < maxY)
				return {x: minX, y: minXy};
		}

		if (x >= midX) { // check "right" side
			var maxXy = m * (maxX - x) + y;
			if (minY < maxXy && maxXy < maxY)
				return {x: maxX, y: maxXy};
		}

		if (y <= midY) { // check "top" side
			var minYx = (minY - y) / m + x;
			if (minX < minYx && minYx < maxX)
				return {x: minYx, y: minY};
		}

		if (y >= midY) { // check "bottom" side
			var maxYx = (maxY - y) / m + x;
			if (minX < maxYx && maxYx < maxX)
				return {x: maxYx, y: maxY};
		}

		// Should never happen :) If it does, please tell me!
		throw "Cannot find intersection for " + [x,y]
		    + " inside rectangle " + [minY, minY] + " - " + [maxX, maxY] + ".";
	};
}();
