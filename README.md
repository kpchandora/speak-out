![](media/SpeakOutHeader.jpg)
# SpeakOut✨
[![GitHub license](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Speakout** lets you upload text posts. The user can add tags, bookmark posts, like, delete and save posts on their device.

## Setup
Add the below code in app level ```build.gradle``` file of this project.
```groovy
debug {
    ...
    buildConfigField 'String', 'BASE_URL', "\"https://staging-app-speakout.herokuapp.com/\""
}
```

## Contribute
If you want to contribute to this app, you're always welcome!
See [Contributing Guidelines](CONTRIBUTING.md). 


## License
```
MIT License

Copyright (c) 2021 Kalpesh Chandora

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
