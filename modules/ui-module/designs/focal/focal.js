function inPercent(value, total) {
    return (value / total) * 100
}

function inNumber(percent, total) {
    if (percent > 100) {
        throw Error('Invalid number, percent can\'t be more than 100')
    }
    return (percent / 100) * total
}

/**
 * if any of the conditions are true, the position
 * is outside the containment. As we want to know
 * if the position is inside the container we 
 * reverse the result of the set of conditions.
 */
const insideContainment = (x, y, containment) => {
    return !(
        x < (containment.x) ||
        x > (containment.x + containment.width) ||
        y < (containment.y) ||
        y > (containment.y + containment.height)
    )
}

const makeFocalPoint = (initialPos = { x: 50, y: 50 }) => {
    const dot = document.querySelector('.focal-point .dot')
    const previews = document.querySelectorAll("[class*='preview-'] img")
    let pos = initialPos
    let isDown = false;
    let img

    const updateImage = () => {
        img = document.querySelector('.focal-point .controls img').getBoundingClientRect()
    }

    const updatePosition = (e) => {
        const x = Math.round(inPercent(e.clientX - img.x, img.width))
        const y = Math.round(inPercent(e.clientY - img.y, img.height))

        if (x <= 0) {
            pos.x = 0
        } else if (x >= 100) {
            pos.x = 100
        } else {
            pos.x = x
        }

        if (y <= 0) {
            pos.y = 0
        } else if (y >= 100) {
            pos.y = 100
        } else {
            pos.y = y
        }
    }

    const updateDot = () => {
        dot.style.left = (inNumber(pos.x, img.width) - (dot.offsetWidth / 2)) + 'px'
        dot.style.top = (inNumber(pos.y, img.height) - (dot.offsetHeight / 2)) + 'px'
    }

    const updatePreviews = () => {
        previews.forEach(preview => {
            preview.style.objectPosition = `${pos.x}% ${pos.y}%`
        })
    }

    const insideImage = (e) => {
        return insideContainment(e.clientX, e.clientY, img)
    }

    const handleImageDown = (e) => {
        if (!insideImage(e)) {
            return
        }

        isDown = true
        updatePosition(e)
        updateDot()
        updatePreviews()
    }

    const handleImageUp = () => {
        isDown = false
    }

    const handleDotMove = (e) => {
        if (!isDown) {
            return
        }

        updatePosition(e)
        updateDot()
        updatePreviews()
    }

    const handleWindowResize = () => {
        updateImage()
        updateDot()
        updatePreviews()
    }

    const init = (initialCoords) => {
        window.addEventListener('resize', handleWindowResize)
        document.addEventListener('mousedown', handleImageDown, true)
        document.addEventListener('mouseup', handleImageUp, true)
        document.addEventListener('mousemove', handleDotMove, true)

        updateImage()
        updateDot()
        updatePreviews()
    }

    init()
}

document.addEventListener('DOMContentLoaded', () => {
    makeFocalPoint()
})