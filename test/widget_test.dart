import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:kepoih/main.dart';

void main() {
  testWidgets('App smoke test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const KepoIhApp());

    // Verify that our app starts correctly by checking for the AppBar title.
    expect(find.text('KepoIh Privacy'), findsOneWidget);
  });
}
