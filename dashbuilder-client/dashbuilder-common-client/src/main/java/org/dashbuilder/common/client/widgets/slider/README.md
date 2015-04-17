GWT Slider
==========

The slider widget implementation located at this package is a copy of the [gwt-slide-bar](http://code.google.com/p/gwt-slider-bar/), at its version <code>1.0</code>.

Usage
------

Check official wiki at [GWT slide bar - Usage](http://code.google.com/p/gwt-slider-bar/wiki/Usage) and [GWT slide bar - Information](http://code.google.com/p/gwt-slider-bar/wiki/CommonInformation)

**Horizontal slider**

    HorizontalSlider slider = new HorizontalSlider(1000, "300px", true);
    slider.addBarValueChangedHandler(new BarValueChangedHandler() {
        @Override
        public void onBarValueChanged(BarValueChangedEvent event) {
            GWT.log("slider value = " + event.getValue());
        }
    });
    slider.drawMarks("white", 6);
    slider.setMinMarkStep(3);
    slider.setNotSelectedInFocus();

**Vertical slider**

    VerticalSlider slider = new VerticalSlider(1000, "300px", true);
    slider.addBarValueChangedHandler(new BarValueChangedHandler() {
        @Override
        public void onBarValueChanged(BarValueChangedEvent event) {
            GWT.log("slider value = " + event.getValue());
        }
    });
    slider.drawMarks("white", 6);
    slider.setMinMarkStep(3);
    slider.setNotSelectedInFocus();

**Triangle slider**

    TriangleSlider slider = new TriangleSlider(1000, "300px", true);
    slider.addBarValueChangedHandler(new BarValueChangedHandler() {
        @Override
        public void onBarValueChanged(BarValueChangedEvent event) {
            GWT.log("slider value = " + event.getValue());
        }
    });
    slider.drawMarks("white", 6);
    slider.setMinMarkStep(3);
    slider.setNotSelectedInFocus();
