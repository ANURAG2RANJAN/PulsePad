import Foundation
import Combine

final class NetworkService: ObservableObject {
    static let shared = NetworkService()
    
    @Published var isConnected: Bool = false
    @Published var packetRate: Int = 0
    @Published var totalPackets: Int = 0
    @Published var serverAddress: String = UserDefaults.standard.string(forKey: "server_address") ?? "192.168.1.7:8080"
    
    private var hzCounter: Int = 0
    private var timer: Timer?
    private let session: URLSession
    
    private init() {
        let config = URLSessionConfiguration.ephemeral
        config.timeoutIntervalForRequest = 1.0
        config.waitsForConnectivity = false
        self.session = URLSession(configuration: config)
        
        startMonitoring()
    }
    
    func updateServerAddress(_ newAddress: String) {
        serverAddress = newAddress.replacingOccurrences(of: "http://", with: "").replacingOccurrences(of: "https://", with: "").trimmingCharacters(in: .whitespacesAndNewlines)
        UserDefaults.standard.set(serverAddress, forKey: "server_address")
        checkPing()
    }
    
    private func startMonitoring() {
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            guard let self = self else { return }
            self.packetRate = self.hzCounter
            self.hzCounter = 0
            self.checkPing()
        }
        checkPing()
    }
    
    func checkPing() {
        guard let url = URL(string: "http://\(serverAddress)/api/ping") else { return }
        
        session.dataTask(with: url) { [weak self] _, response, error in
            DispatchQueue.main.async {
                if let http = response as? HTTPURLResponse, http.statusCode == 200 {
                    self?.isConnected = true
                } else {
                    self?.isConnected = false
                }
            }
        }.resume()
    }
    
    func send<T: Codable>(endpoint: String, payload: T) {
        guard let url = URL(string: "http://\(serverAddress)/api/\(endpoint)") else { return }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        do {
            request.httpBody = try JSONEncoder().encode(payload)
        } catch {
            return
        }
        
        totalPackets += 1
        hzCounter += 1
        
        session.dataTask(with: request) { [weak self] _, response, error in
            if let http = response as? HTTPURLResponse, http.statusCode == 200 {
                DispatchQueue.main.async {
                    self?.isConnected = true
                }
            }
        }.resume()
    }
}
