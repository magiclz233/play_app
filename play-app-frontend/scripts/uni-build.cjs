process.env.CI = process.env.CI || '1';
process.env.VITE_CJS_IGNORE_WARNING = 'true';

require('@dcloudio/vite-plugin-uni/bin/uni.js');
