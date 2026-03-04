import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  runApp(const KepoIhApp());
}

class KepoIhApp extends StatelessWidget {
  const KepoIhApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'KepoIh',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
            seedColor: Colors.deepPurple, brightness: Brightness.dark),
        useMaterial3: true,
      ),
      home: const SettingsPage(),
    );
  }
}

class SettingsPage extends StatefulWidget {
  const SettingsPage({super.key});

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  static const platform = MethodChannel('com.drestaputra.kepoih/privacy');

  bool _isServiceRunning = false;
  double _sensitivityLevel = 1.2;

  @override
  void initState() {
    super.initState();
    _checkServiceStatus();
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _sensitivityLevel = prefs.getDouble('sensitivity') ?? 1.2;
    });
  }

  Future<void> _saveSettings() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setDouble('sensitivity', _sensitivityLevel);
  }

  Future<void> _checkServiceStatus() async {
    try {
      final bool isRunning = await platform.invokeMethod('isServiceRunning');
      setState(() {
        _isServiceRunning = isRunning;
      });
    } on PlatformException catch (e) {
      debugPrint("Failed to get service status: '${e.message}'.");
    }
  }

  Future<void> _toggleService() async {
    if (_isServiceRunning) {
      await _stopService();
    } else {
      await _startService();
    }
  }

  Future<void> _startService() async {
    if (!await Permission.systemAlertWindow.isGranted) {
      final status = await Permission.systemAlertWindow.request();
      if (!status.isGranted) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
                content:
                    Text('Overlay permission is required to blur the screen.')),
          );
        }
        return;
      }
    }

    try {
      await platform.invokeMethod('startService', {'sensitivity': _sensitivityLevel});
      setState(() {
        _isServiceRunning = true;
      });
    } on PlatformException catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error starting protection: ${e.message}')),
        );
      }
    }
  }

  Future<void> _stopService() async {
    try {
      await platform.invokeMethod('stopService');
      setState(() {
        _isServiceRunning = false;
      });
    } on PlatformException catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error stopping protection: ${e.message}')),
        );
      }
    }
  }

  String _getSensitivityLabel(double value) {
    if (value < 1.0) return 'Low';
    if (value < 1.8) return 'Medium';
    return 'High';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('KepoIh Privacy'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Icon(
                _isServiceRunning ? Icons.security : Icons.gpp_maybe,
                size: 100,
                color: _isServiceRunning ? Colors.green : Colors.red,
              ),
            ),
            const SizedBox(height: 16),
            Center(
              child: Text(
                _isServiceRunning
                    ? 'Protection Active'
                    : 'Protection Inactive',
                style: Theme.of(context).textTheme.headlineSmall,
              ),
            ),
            const SizedBox(height: 48),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text('Toggle Privacy Protection',
                        style: TextStyle(fontSize: 16)),
                    Switch(
                      value: _isServiceRunning,
                      onChanged: (value) => _toggleService(),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),
            const Text('Sensitivity',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Text(
              'Current: ${_getSensitivityLabel(_sensitivityLevel)}',
              style: TextStyle(color: Colors.grey[400]),
            ),
            Slider(
              value: _sensitivityLevel,
              min: 0.5,
              max: 2.5,
              divisions: 10,
              label: _getSensitivityLabel(_sensitivityLevel),
              onChanged: _isServiceRunning
                  ? null // Disable slider when service is running
                  : (value) {
                      setState(() {
                        _sensitivityLevel = value;
                      });
                    },
              onChangeEnd: (value) {
                _saveSettings();
              },
            ),
            if (_isServiceRunning)
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 16.0),
                child: Text(
                  'Stop protection to adjust sensitivity.',
                  style: TextStyle(color: Colors.amber, fontSize: 12),
                ),
              )
          ],
        ),
      ),
    );
  }
}